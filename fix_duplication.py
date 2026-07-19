with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "r") as f:
    lines = f.readlines()

# The good file part 1 is lines 0 to 1813 (0-indexed 0 to 1812)
part1 = lines[:1813]

# The original file starts repeating at line 1814 (index 1813)
duplicate_start = 1813

# Inside the duplicated part, let's find the old `fun SubjectGradeGridCard`
idx = duplicate_start
while idx < len(lines):
    if lines[idx].startswith("fun SubjectGradeGridCard("):
        break
    idx += 1

# Now find the END of the old SubjectGradeGridCard
# We can find it by looking for the next top-level function or similar?
# Or just count braces.
def find_brace_end(start_idx, lines_list):
    open_count = 0
    for i in range(start_idx, len(lines_list)):
        open_count += lines_list[i].count('{')
        open_count -= lines_list[i].count('}')
        if open_count == 0 and i > start_idx:
            return i
    return -1

end_idx = find_brace_end(idx, lines)

if end_idx != -1:
    part2 = lines[end_idx + 1:]
    with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "w") as f:
        f.writelines(part1)
        f.writelines(part2)
    print("Fixed duplication!")
else:
    print("Failed to find end of function")

