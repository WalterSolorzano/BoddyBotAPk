import re

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyNavGraph.kt", "r") as f:
    content = f.read()

# Add import
if "import com.aistudio.unibuddy.qywvsp.ui.screens.SeasonRecapOverlay" not in content:
    content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\nimport com.aistudio.unibuddy.qywvsp.ui.screens.SeasonRecapOverlay\nimport androidx.compose.runtime.collectAsState\nimport androidx.compose.runtime.getValue")

# Add overlay after NavHost
old_navhost = """        }
    )
}"""
new_navhost = """        }
    )

    val recapToShow by viewModel.showSeasonRecap.collectAsState()
    recapToShow?.let { recap ->
        SeasonRecapOverlay(
            recap = recap,
            onDismiss = { viewModel.dismissSeasonRecap() }
        )
    }
}"""
content = content.replace(old_navhost, new_navhost)

with open("app/src/main/java/com/aistudio/unibuddy/qywvsp/ui/UniBuddyNavGraph.kt", "w") as f:
    f.write(content)

