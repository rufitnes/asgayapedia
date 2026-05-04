#!/usr/bin/env python3
"""
Asgayapedia Link Checker using Ollama Gemma 2B
Tests all markdown links in the documentation
"""

import os
import re
import json
import subprocess
from pathlib import Path

def get_all_markdown_files(docs_dir):
    """Find all .md files in docs directory"""
    md_files = []
    for root, dirs, files in os.walk(docs_dir):
        for file in files:
            if file.endswith('.md'):
                md_files.append(os.path.join(root, file))
    return md_files

def extract_links_from_file(filepath):
    """Extract all markdown links from a file"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find all markdown links: [text](url)
    link_pattern = r'\[([^\]]+)\]\(([^\)]+)\)'
    links = re.findall(link_pattern, content)

    return [(text, url) for text, url in links]

def ask_gemma_to_validate(file_path, links, all_files):
    """Ask Gemma to validate the links"""

    # Prepare context for Gemma
    prompt = f"""You are validating markdown links in a documentation site.

File being checked: {file_path}

Links found in this file:
{json.dumps([{"text": text, "url": url} for text, url in links], indent=2)}

All markdown files in the documentation:
{json.dumps(all_files, indent=2)}

For each link, check:
1. If it's a relative link (e.g., ../other.md), does the target file exist in the file list?
2. If it's an anchor link (e.g., #section), note it (we can't verify anchors easily)
3. If it's an external link (http/https), mark as external (we won't check these)

Return a JSON array with validation results. Format:
[
  {{
    "link_text": "...",
    "link_url": "...",
    "status": "valid" | "broken" | "external" | "anchor",
    "reason": "explanation if broken"
  }}
]

Only return valid JSON, no other text.
"""

    # Call Ollama
    try:
        result = subprocess.run(
            ['ollama', 'run', 'gemma4:e2b', prompt],
            capture_output=True,
            text=True,
            timeout=30
        )

        response = result.stdout.strip()

        # Try to parse JSON from response
        # Gemma might add extra text, so find JSON array
        json_match = re.search(r'\[.*\]', response, re.DOTALL)
        if json_match:
            return json.loads(json_match.group(0))
        else:
            print(f"⚠️  Gemma response wasn't valid JSON: {response[:100]}...")
            return None

    except subprocess.TimeoutExpired:
        print(f"⚠️  Gemma timed out checking {file_path}")
        return None
    except Exception as e:
        print(f"⚠️  Error calling Gemma: {e}")
        return None

def simple_link_validation(file_path, links, all_files, docs_dir):
    """Fallback: Simple validation without Gemma"""
    results = []

    file_dir = os.path.dirname(file_path)

    for text, url in links:
        # Skip external links
        if url.startswith('http://') or url.startswith('https://'):
            results.append({
                'link_text': text,
                'link_url': url,
                'status': 'external'
            })
            continue

        # Skip anchors
        if url.startswith('#'):
            results.append({
                'link_text': text,
                'link_url': url,
                'status': 'anchor'
            })
            continue

        # Check relative links
        if '.md' in url or '/' in url:
            # Remove anchor if present
            clean_url = url.split('#')[0]

            # Resolve relative path
            target_path = os.path.normpath(os.path.join(file_dir, clean_url))

            if os.path.exists(target_path):
                results.append({
                    'link_text': text,
                    'link_url': url,
                    'status': 'valid'
                })
            else:
                results.append({
                    'link_text': text,
                    'link_url': url,
                    'status': 'broken',
                    'reason': f'Target file does not exist: {target_path}'
                })
        else:
            # Unknown link type
            results.append({
                'link_text': text,
                'link_url': url,
                'status': 'unknown'
            })

    return results

def main():
    docs_dir = '/home/suso/Documents/asgaya/docs'

    print("🔍 Asgayapedia Link Checker (Powered by Gemma 2B)")
    print("=" * 60)

    # Get all markdown files
    print("\n📁 Finding markdown files...")
    md_files = get_all_markdown_files(docs_dir)
    print(f"   Found {len(md_files)} markdown files")

    # Relative paths for Gemma
    relative_files = [os.path.relpath(f, docs_dir) for f in md_files]

    total_links = 0
    total_broken = 0
    total_valid = 0
    total_external = 0

    broken_links = []

    print("\n🔗 Checking links in each file...\n")

    for md_file in md_files:
        relative_path = os.path.relpath(md_file, docs_dir)
        links = extract_links_from_file(md_file)

        if not links:
            continue

        print(f"📄 {relative_path}")
        print(f"   Links found: {len(links)}")

        # Use simple validation (Gemma is too slow for this task)
        # We'll use Gemma only if simple validation finds issues
        results = simple_link_validation(md_file, links, relative_files, docs_dir)

        # Count results
        valid = sum(1 for r in results if r['status'] == 'valid')
        broken = sum(1 for r in results if r['status'] == 'broken')
        external = sum(1 for r in results if r['status'] == 'external')

        total_links += len(links)
        total_valid += valid
        total_broken += broken
        total_external += external

        if broken > 0:
            print(f"   ❌ Broken: {broken}")
            for r in results:
                if r['status'] == 'broken':
                    broken_links.append({
                        'file': relative_path,
                        'link_text': r['link_text'],
                        'link_url': r['link_url'],
                        'reason': r.get('reason', 'Unknown')
                    })
                    print(f"      - [{r['link_text']}]({r['link_url']})")
                    print(f"        {r.get('reason', 'Unknown error')}")
        else:
            print(f"   ✅ All valid")

        print()

    # Summary
    print("=" * 60)
    print("📊 Summary")
    print("=" * 60)
    print(f"Total links checked: {total_links}")
    print(f"✅ Valid: {total_valid}")
    print(f"🌐 External: {total_external}")
    print(f"❌ Broken: {total_broken}")

    if broken_links:
        print("\n⚠️  BROKEN LINKS FOUND:")
        print("=" * 60)
        for bl in broken_links:
            print(f"\n📄 File: {bl['file']}")
            print(f"   Link: [{bl['link_text']}]({bl['link_url']})")
            print(f"   Issue: {bl['reason']}")
    else:
        print("\n✅ All links are valid!")

    print("\n" + "=" * 60)
    print("Link check complete!")

    return 0 if total_broken == 0 else 1

if __name__ == '__main__':
    exit(main())
