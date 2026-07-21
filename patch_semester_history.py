import sys

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/SemesterHistoryView.kt", "r") as f:
    lines = f.readlines()

out_lines = []
skip = False
brace_count = 0

for line in lines:
    if "// Grade Goals Calculator" in line:
        skip = True
        
    if skip:
        brace_count += line.count("{")
        brace_count -= line.count("}")
        if brace_count == 0 and "}" in line:
            # We assume this is the end of the `if (pendingExamsCount > 0)` or the enclosing block, but wait, the `// Grade Goals Calculator` is followed by `val pendingExamsCount` then `if (pendingExamsCount > 0)`.
            # Let's just remove everything from `// Grade Goals Calculator` to the exact matching end.
            pass

# simpler approach: just find lines to remove
start_line = -1
end_line = -1
for i, line in enumerate(lines):
    if "// Grade Goals Calculator" in line:
        start_line = i
    if start_line != -1 and "Tu Meta de Promedio" in line:
        pass

