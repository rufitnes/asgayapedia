#!/bin/bash
# Generate audio files from Asgaya documentation
# One MP3 per markdown file for in-car review

set -e

# Configuration
VOICE="${TTS_VOICE:-en-US-GuyNeural}"  # Change to en-GB-RyanNeural, es-ES-AlvaroNeural, etc.
RATE="${TTS_RATE:-+0%}"  # Speed: -50% to +100% (-20% = slower, +20% = faster)
OUTPUT_DIR="asgaya-audio"
DOCS_DIR="docs"

# Create output directory
mkdir -p "$OUTPUT_DIR"

echo "🎙️  Asgaya Documentation → Audio Conversion"
echo "Voice: $VOICE"
echo "Rate: $RATE"
echo ""

# Check if edge-tts is installed
if ! command -v edge-tts &> /dev/null; then
    echo "❌ edge-tts not found. Install with: pip install edge-tts"
    exit 1
fi

# Function to clean markdown for TTS (remove formatting symbols)
clean_markdown() {
    local input_file="$1"
    local temp_file=$(mktemp)

    # Process markdown to plain text for natural TTS
    cat "$input_file" | \
        # Remove HTML comments
        sed 's/<!--.*-->//g' | \
        # Remove code blocks (replace with placeholder)
        sed '/```/,/```/c\[Code example omitted for audio]' | \
        # Remove inline code backticks
        sed 's/`//g' | \
        # Remove bold/italic markers but keep text
        sed 's/\*\*\([^*]*\)\*\*/\1/g' | \
        sed 's/\*\([^*]*\)\*/\1/g' | \
        sed 's/__\([^_]*\)__/\1/g' | \
        sed 's/_\([^_]*\)_/\1/g' | \
        # Remove strikethrough
        sed 's/~~\([^~]*\)~~/\1/g' | \
        # Convert headers (remove # symbols, keep just the text)
        sed 's/^######* //g' | \
        # Convert links [text](url) to just text
        sed 's/\[\([^]]*\)\]([^)]*)/\1/g' | \
        # Remove markdown image syntax
        sed 's/!\[\([^]]*\)\]([^)]*)//g' | \
        # Remove horizontal rules
        sed 's/^---*$//g' | \
        # Remove list markers but keep text (-, *, +, numbers)
        sed 's/^[[:space:]]*[-*+] /  /g' | \
        sed 's/^[[:space:]]*[0-9]\+\. /  /g' | \
        # Remove table formatting (keep text, remove pipes)
        sed 's/|/ /g' | \
        # Remove blockquote markers
        sed 's/^> //g' | \
        # Add natural pauses at section breaks (double newlines)
        sed 's/^$/\. /g' | \
        # Clean up multiple spaces
        tr -s ' ' \
        > "$temp_file"

    echo "$temp_file"
}

# Function to convert markdown to audio
convert_file() {
    local md_file="$1"
    local counter="$2"

    # Generate clean filename: 01-core-architecture-README.mp3
    # Remove docs/ prefix, replace / with -, remove .md
    local base_name=$(echo "$md_file" | sed 's|^docs/||' | sed 's|/|-|g' | sed 's|\.md$||')
    local audio_file=$(printf "%03d-%s.mp3" "$counter" "$base_name")
    local output_path="$OUTPUT_DIR/$audio_file"

    echo "[$counter] Converting: $md_file → $audio_file"

    # Clean markdown formatting for natural TTS
    local clean_file=$(clean_markdown "$md_file")

    # Convert to audio (suppress progress bar for cleaner output)
    edge-tts --voice "$VOICE" --rate="$RATE" --file "$clean_file" --write-media "$output_path" 2>/dev/null

    if [ $? -eq 0 ]; then
        echo "    ✓ Generated: $output_path"
        # Clean up temp file
        rm -f "$clean_file"
    else
        echo "    ✗ Failed: $md_file"
        rm -f "$clean_file"
    fi
}

# Priority files for 4-5 hour drive (most important first)
PRIORITY_FILES=(
    "docs/index.md"
    "docs/core-architecture/README.md"
    "docs/concepts/README.md"
    "docs/concepts/risk-allocation-principle.md"
    "docs/concepts/overcollateralized-bounty-contracts.md"
    "docs/concepts/bch-sellers.md"
    "docs/concepts/pull-system.md"
    "docs/decisions/README.md"
    "docs/decisions/two-step-settlement-timing.md"
    "docs/decisions/fee-splitting-model.md"
    "docs/unknowns/README.md"
    "docs/android-app/README.md"
    "docs/android-app/flows/README.md"
)

# Mode selection
MODE="${1:-priority}"

case "$MODE" in
    priority)
        echo "📋 Mode: Priority files only (~2 hours)"
        echo ""
        counter=1
        for file in "${PRIORITY_FILES[@]}"; do
            if [ -f "$file" ]; then
                convert_file "$file" "$counter"
                counter=$((counter + 1))
            fi
        done
        ;;

    all)
        echo "📋 Mode: All markdown files (~10+ hours)"
        echo ""
        counter=1
        while IFS= read -r file; do
            convert_file "$file" "$counter"
            counter=$((counter + 1))
        done < <(find "$DOCS_DIR" -name "*.md" -type f | grep -v node_modules | sort)
        ;;

    custom)
        echo "📋 Mode: Custom file list"
        echo "Usage: echo 'docs/file1.md' | $0 custom"
        counter=1
        while IFS= read -r file; do
            if [ -f "$file" ]; then
                convert_file "$file" "$counter"
                counter=$((counter + 1))
            fi
        done
        ;;

    *)
        echo "❌ Unknown mode: $MODE"
        echo ""
        echo "Usage: $0 [priority|all|custom]"
        echo ""
        echo "Modes:"
        echo "  priority - Entry points + core concepts (~2 hours)"
        echo "  all      - Every markdown file (~10+ hours)"
        echo "  custom   - Pipe file list via stdin"
        echo ""
        echo "Environment variables:"
        echo "  TTS_VOICE - Voice to use (default: en-US-GuyNeural)"
        echo "  TTS_RATE  - Speed adjustment (default: +0%)"
        echo ""
        echo "Examples:"
        echo "  $0 priority                    # Generate priority files"
        echo "  $0 all                         # Generate all files"
        echo "  TTS_RATE='+20%' $0 priority    # 20% faster"
        echo "  TTS_VOICE='en-GB-RyanNeural' $0 all  # British accent"
        exit 1
        ;;
esac

# Generate M3U playlist
echo ""
echo "📝 Generating playlist..."
ls -1 "$OUTPUT_DIR"/*.mp3 2>/dev/null | sed "s|$OUTPUT_DIR/||" > "$OUTPUT_DIR/playlist.m3u"

echo ""
echo "✅ Conversion complete!"
echo ""
echo "📊 Stats:"
echo "   Files: $(ls -1 "$OUTPUT_DIR"/*.mp3 2>/dev/null | wc -l)"
echo "   Size:  $(du -sh "$OUTPUT_DIR" | cut -f1)"
echo ""
echo "📱 Next steps:"
echo "   1. Copy $OUTPUT_DIR/ to phone/USB"
echo "   2. Load playlist.m3u in car audio system"
echo "   3. File names will show in HUD"
echo "   4. Pause + voice note when you catch issues"
echo ""
echo "🎧 Available voices: edge-tts --list-voices | grep Neural"
