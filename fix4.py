import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

# Replace all } directly preceding fun checkpointDatabase with nothing, keeping only what's necessary to close the previous method
# The previous method ends with:
#             }
#         }
#     }

target = r"\}\s*\}\s*\}\s*\}\s*fun checkpointDatabase"
replacement = "            }\n        }\n    }\n    fun checkpointDatabase"
content = re.sub(target, replacement, content)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)

