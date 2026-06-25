const fs = require('fs');
const path = require('path');

function replaceInDir(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            replaceInDir(fullPath);
        } else if (fullPath.endsWith('.kt')) {
            let content = fs.readFileSync(fullPath, 'utf8');
            let modified = false;

            if (content.includes('sessionsJson.parseSessions()')) {
                content = content.replace(/sessionsJson\.parseSessions\(\)/g, 'sessions');
                modified = true;
            }
            if (content.includes('sessionsJson?.parseSessions()')) {
                content = content.replace(/sessionsJson\?\.parseSessions\(\)/g, 'sessions');
                modified = true;
            }
            if (content.includes('sessionsJson?.let { it.parseSessions() }')) {
                content = content.replace(/sessionsJson\?\.let \{ it\.parseSessions\(\) \}/g, 'sessions');
                modified = true;
            }
            if (content.includes('JSONArray(sub.sessionsJson)')) {
                content = content.replace(/JSONArray\(sub\.sessionsJson\)/g, 'null /* removed */');
                modified = true;
            }
            if (content.includes('sessionsJson = jsonString')) {
                content = content.replace(/sessionsJson = jsonString/g, 'sessions = emptyList() /* removed jsonString */');
                modified = true;
            }

            if (modified) {
                fs.writeFileSync(fullPath, content);
                console.log('Modified ' + fullPath);
            }
        }
    }
}

replaceInDir('app/src/main/java/com/aistudio/unibuddy/qywvsp');
