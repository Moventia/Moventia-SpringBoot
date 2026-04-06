const fs = require('fs');
const path = require('path');

const srcDir = path.join(__dirname, 'src');

function walk(dir) {
  let results = [];
  const list = fs.readdirSync(dir);
  list.forEach(file => {
    file = path.join(dir, file);
    const stat = fs.statSync(file);
    if (stat && stat.isDirectory()) {
      results = results.concat(walk(file));
    } else if (file.endsWith('.jsx')) {
      results.push(file);
    }
  });
  return results;
}

const files = walk(srcDir);

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  let changed = false;

  if (content.includes(`const API_URL = 'http://localhost:8080/api/auth';`)) {
    content = content.replace(
      `const API_URL = 'http://localhost:8080/api/auth';`,
      "const API_URL = `${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/auth`;"
    );
    changed = true;
  }
  
  if (content.includes(`const API_URL = 'http://localhost:8080/api';`)) {
    content = content.replace(
      `const API_URL = 'http://localhost:8080/api';`,
      "const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';"
    );
    changed = true;
  }
  
  if (content.includes("http://localhost:8080${data.avatarUrl}")) {
    content = content.replace(
      "http://localhost:8080${data.avatarUrl}",
      "${import.meta.env.VITE_API_URL ? import.meta.env.VITE_API_URL.replace('/api', '') : 'http://localhost:8080'}${data.avatarUrl}"
    );
    changed = true;
  }

  if (changed) {
    fs.writeFileSync(file, content, 'utf8');
  }
});

console.log('Update complete');
