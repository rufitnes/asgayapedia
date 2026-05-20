#!/bin/bash
# Phase 2: Context-Dependent Terminology Substitutions
# Promise → Cash buy order, Top-up → Time extension, Maturity → Expiry

set -e

DOCS_DIR="docs"
DRY_RUN="${1:-false}"

echo "🔄 Phase 2: Context-Dependent Substitutions"
echo "============================================"
echo ""

if [ "$DRY_RUN" = "dry-run" ]; then
    echo "📋 DRY RUN MODE - No files will be modified"
    echo ""
fi

# Function to apply substitutions
substitute_file() {
    local file="$1"
    local temp_file=$(mktemp)

    # Apply substitutions
    sed \
        -e 's/EUR-denominated promise/EUR-denominated cash buy order/g' \
        -e 's/EUR-denominated promises/EUR-denominated cash buy orders/g' \
        -e 's/Covenant promises/Covenant holds cash buy order for/g' \
        -e 's/covenant promises/covenant holds cash buy order for/g' \
        -e 's/promised EUR value/specified EUR value/g' \
        -e 's/Promised EUR value/Specified EUR value/g' \
        -e 's/top-up opportunity/time extension opportunity/g' \
        -e 's/Top-up opportunity/Time extension opportunity/g' \
        -e 's/top-up/time extension/g' \
        -e 's/Top-up/Time extension/g' \
        -e 's/top up/time extension/g' \
        -e 's/Top up/Time extension/g' \
        -e 's/early maturity/early expiry/g' \
        -e 's/Early maturity/Early expiry/g' \
        -e 's/matures early/expires early/g' \
        -e 's/Matures early/Expires early/g' \
        -e 's/maturity rate/settlement rate/g' \
        -e 's/Maturity rate/Settlement rate/g' \
        -e 's/at maturity/at settlement/g' \
        -e 's/At maturity/At settlement/g' \
        "$file" > "$temp_file"

    # Check if file changed
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
echo "  'cash buy order': $(grep -r "cash buy order" --include="*.md" . | wc -l) instances"
echo "  'time extension': $(grep -r "time extension" --include="*.md" . | wc -l) instances"
echo "  'early expiry': $(grep -r "early expiry" --include="*.md" . | wc -l) instances"
echo "  'settlement rate': $(grep -r "settlement rate" --include="*.md" . | wc -l) instances"
cd ..

echo ""
if [ "$DRY_RUN" = "dry-run" ]; then
    echo "✅ DRY RUN COMPLETE - Run without 'dry-run' to apply"
else
    echo "✅ PHASE 2 COMPLETE - Review with git diff"
    echo ""
    echo "📝 Next: Create time-extension-marketplace.md concept file"
fi
