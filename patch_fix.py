with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "r") as f:
    content = f.read()

bad_part = """@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)

@Entity(tableName = "season_recaps")
data class SeasonRecap"""

fixed_part = """@Entity(tableName = "season_recaps")
data class SeasonRecap"""

content = content.replace(bad_part, fixed_part)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "w") as f:
    f.write(content)

