with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "r") as f:
    lines = f.readlines()

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/HeroNextClassCard.kt", "w") as f:
    for i, line in enumerate(lines):
        if i == 123 and line.strip() == "}":
            continue
        f.write(line)
