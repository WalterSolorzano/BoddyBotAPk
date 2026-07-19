import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyApp.kt", "r") as f:
    content = f.read()

pattern = r"""                                val chunkedSubjects = subjects\.chunked\(2\)
                                items\(chunkedSubjects, key = \{ it\.first\(\)\.id \}\) \{ pair ->(.*?)(?=\n                                    Row\()"""
# Wait, let's use a simpler string replace or regex.
