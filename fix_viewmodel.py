import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "r") as f:
    content = f.read()

# First, remove the bad insertion at the end of the file
bad_code = """    fun checkpointDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.db.query(androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun exportDatabaseToUri(context: android.content.Context, uri: android.net.Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                repository.db.query(androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
                val dbFile = context.getDatabasePath("unibuddy_database")
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    dbFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importDatabaseFromUri(context: android.content.Context, uri: android.net.Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dbFile = context.getDatabasePath("unibuddy_database")
                val walFile = context.getDatabasePath("unibuddy_database-wal")
                val shmFile = context.getDatabasePath("unibuddy_database-shm")
                
                // Close the DB to allow replacement
                repository.db.close()
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Delete wal and shm since we replaced the main db file
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}"""
content = content.replace(bad_code, "")

# Now find the end of UniBuddyViewModel class
# UniBuddyViewModel ends right before "class UniBuddyViewModelFactory"
match = re.search(r'(\s+)(class UniBuddyViewModelFactory)', content)
if match:
    correct_insertion = """    fun checkpointDatabase() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.db.query(androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun exportDatabaseToUri(context: android.content.Context, uri: android.net.Uri): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.db.query(androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
                val dbFile = context.getDatabasePath("unibuddy_database")
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    dbFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importDatabaseFromUri(context: android.content.Context, uri: android.net.Uri): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val dbFile = context.getDatabasePath("unibuddy_database")
                val walFile = context.getDatabasePath("unibuddy_database-wal")
                val shmFile = context.getDatabasePath("unibuddy_database-shm")
                
                // Close the DB to allow replacement
                repository.db.close()
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Delete wal and shm since we replaced the main db file
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
"""
    content = content[:match.start()] + correct_insertion + content[match.start():]

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyViewModel.kt", "w") as f:
    f.write(content)

