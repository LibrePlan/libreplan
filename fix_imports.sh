#!/bin/bash

# Find all Java files that use _t but don't have the proper import
find libreplan-business/src/main/java -name "*.java" -exec grep -l "_t(" {} \; | while read file; do
    # Check if file already has the import
    if ! grep -q "import.*I18nHelper" "$file"; then
        # Find the package declaration line number
        package_line=$(grep -n "^package" "$file" | cut -d: -f1)
        
        # Add import after package declaration
        if [ -n "$package_line" ]; then
            next_line=$((package_line + 1))
            # Insert import statement
            sed -i "${next_line}a import org.libreplan.business.i18n.I18nHelper;" "$file"
            echo "Added import to: $file"
        fi
        
        # Replace _t( with I18nHelper._t(
        sed -i 's/_t(/I18nHelper._t(/g' "$file"
    fi
done

echo "Import fix completed!"
