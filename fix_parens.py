with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "r") as f:
    lines = f.readlines()

def insert_paren(line_num):
    # line_num is 1-indexed
    lines.insert(line_num - 1, "            )\n")

# Order must be descending so line numbers don't shift!
# We only want to fix the ones reported:
# 1218, 668, 578, 563, 520, 514, 509, 504, 412
errors = [1218, 668, 578, 563, 520, 514, 509, 504, 412]

for err in errors:
    insert_paren(err)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/screens/DashboardScreen.kt", "w") as f:
    f.writelines(lines)
