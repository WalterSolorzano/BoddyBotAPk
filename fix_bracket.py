with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "r") as f:
    content = f.read()

content = content.replace("        }        }\n    }\n\n    // Determine attendance urgency decision", "        }\n    }\n\n    // Determine attendance urgency decision")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "w") as f:
    f.write(content)
