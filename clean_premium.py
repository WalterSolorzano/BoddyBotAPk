import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/PremiumWidgets.kt", "r") as f:
    content = f.read()

# Fix import
content = content.replace("import androidx.glance.action.actionRunCallback", "import androidx.glance.appwidget.action.actionRunCallback")

# Remove bottom duplicated class (the one I appended with test_compile_action.py)
duplicate_class_idx = content.rfind("class RefreshCommuteAction : ActionCallback {", 0, content.rfind("class RefreshCommuteAction : ActionCallback {"))
if duplicate_class_idx != -1:
    # Actually wait, let's just find the last class and remove it.
    idx = content.rfind("class RefreshCommuteAction : ActionCallback")
    if idx > 0:
        # We need to remove the first one maybe?
        pass

# Let's just remove anything after the FIRST "class RefreshCommuteAction : ActionCallback {" block.
lines = content.split('\n')
out_lines = []
found_first = False
in_first = False
brace_count = 0

for line in lines:
    if "class RefreshCommuteAction" in line:
        if not found_first:
            found_first = True
            in_first = True
            out_lines.append(line)
            brace_count += line.count('{') - line.count('}')
        else:
            # Skip this line and everything after it? We will just break when we finish the first class.
            pass
    elif in_first:
        out_lines.append(line)
        brace_count += line.count('{') - line.count('}')
        if brace_count == 0:
            in_first = False
            # Break so we don't include the trailing stuff I appended by accident
            break
    else:
        if not found_first:
            out_lines.append(line)
        else:
            # We already finished the first class, so anything else is garbage I appended.
            pass

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/widget/PremiumWidgets.kt", "w") as f:
    f.write('\n'.join(out_lines))
