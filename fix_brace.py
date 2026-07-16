with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "r") as f:
    content = f.read()

content = content.replace("            }\n        }\n    }\n\n    // Determine", "            }\n        }\n    }\n    }\n\n    // Determine")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "w") as f:
    f.write(content)
