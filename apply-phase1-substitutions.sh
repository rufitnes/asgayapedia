#!/bin/bash
# Phase 1: Safe Global Terminology Substitutions
# Apply Asgaya-specific terminology to all markdown files

set -e

DOCS_DIR="docs"
DRY_RUN="${1:-false}"

echo "🔄 Phase 1: Terminology Substitutions"
echo "======================================"
echo ""

if [ "$DRY_RUN" = "dry-run" ]; then
    echo "📋 DRY RUN MODE - No files will be modified"
    echo ""
fi

# Count files to process
total_files=$(find "$DOCS_DIR" -name "*.md" -type f | grep -v node_modules | wc -l)
echo "📂 Processing $total_files markdown files..."
echo ""

# Track changes
declare -A counts

# Function to apply substitutions to a file
substitute_file() {
    local file="$1"
    local temp_file=$(mktemp)

    # Apply substitutions in order
    sed \
        -e 's/overcollateralization/volatility buffer/g' \
        -e 's/Overcollateralization/Volatility buffer/g' \
        -e 's/overcollateralized BCH/BCH + volatility buffer/g' \
        -e 's/Overcollateralized BCH/BCH + volatility buffer/g' \
        -e 's/overcollateralized covenant/covenant + volatility buffer/g' \
        -e 's/Overcollateralized covenant/Covenant + volatility buffer/g' \
        -e 's/overcollateralized bounty/bounty + volatility buffer/g' \
        -e 's/Overcollateralized bounty/Bounty + volatility buffer/g' \
        -e 's/overcollateralized/with volatility buffer/g' \
        -e 's/Overcollateralized/With volatility buffer/g' \
        -e 's/settlement window/claim window/g' \
        -e 's/Settlement window/Claim window/g' \
        -e 's/reliability reward/uptime incentive/g' \
        -e 's/Reliability reward/Uptime incentive/g' \
        "$file" > "$temp_file"

    # Check if file actually changed
    if ! cmp -s "$file" "$temp_file"; then
        if [ "$DRY_RUN" = "dry-run" ]; then
            echo "  Would modify: $file"
        else
            mv "$temp_file" "$file"
            echo "  ✓ Modified: $file"
        fi
    else
        rm "$temp_file"
    fi
}

# Process all markdown files
while IFS= read -r file; do
    substitute_file "$file"
done < <(find "$DOCS_DIR" -name "*.md" -type f | grep -v node_modules | sort)

echo ""
echo "📊 Substitution Statistics:"
echo "======================================"
cd "$DOCS_DIR"
echo "  'volatility buffer': $(grep -r "volatility buffer" --include="*.md" . | wc -l) instances"
echo "  'claim window': $(grep -r "claim window" --include="*.md" . | wc -l) instances"
echo "  'uptime incentive': $(grep -r "uptime incentive" --include="*.md" . | wc -l) instances"
cd ..

echo ""
echo "🔄 File Rename Required:"
echo "======================================"
if [ -f "$DOCS_DIR/concepts/overcollateralized-bounty-contracts.md" ]; then
    echo "  📄 concepts/overcollateralized-bounty-contracts.md"
    echo "     → concepts/bounty-contracts-with-volatility-buffer.md"
    echo ""
    echo "  Run: git mv docs/concepts/overcollateralized-bounty-contracts.md \\"
    echo "              docs/concepts/bounty-contracts-with-volatility-buffer.md"
else
    echo "  ✓ File already renamed or doesn't exist"
fi

echo ""
if [ "$DRY_RUN" = "dry-run" ]; then
    echo "✅ DRY RUN COMPLETE - Run without 'dry-run' argument to apply changes"
else
    echo "✅ PHASE 1 COMPLETE - Review changes with git diff"
fi
