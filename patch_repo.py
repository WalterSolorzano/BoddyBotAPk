with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "r") as f:
    content = f.read()

content = content.replace("val tasks: Flow<List<Task>> = db.taskDao().getAllTasks()", "val tasks: Flow<List<Task>> = db.taskDao().getAllTasks()\n    val seasonRecaps: Flow<List<SeasonRecap>> = db.seasonRecapDao().getAllRecaps()")
content = content.replace("suspend fun insertTask(task: Task): Long = db.taskDao().insertTask(task)", "suspend fun insertTask(task: Task): Long = db.taskDao().insertTask(task)\n    suspend fun insertSeasonRecap(recap: SeasonRecap): Long = db.seasonRecapDao().insertRecap(recap)")

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/data/Database.kt", "w") as f:
    f.write(content)
