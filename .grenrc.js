module.exports = {
    "dataSource": "prs",
    "prefix": "",
    "onlyMilestones": false,
    "ignoreCommitsWith": ["chore", "refactor", "style", "(bp #"],
    "ignoreIssuesWith": ["no-release"],
    "ignoreTagsWith": ["-rc", "-alpha", "-beta", "test", "current"],
    "ignoreLabels": ["closed", "automation", "enhancement", "bug", "fix",
      "internal", "feature", "feat", "docs", "chore", "refactor", "ci",
      "perf", "test", "tests", "style", "groovy", "linux", "master", "mac", "windows",
      "team:automation", "ready-to-merge", "backport-to-7.x",
      "backport-skip","dependencies", "java", "bump"],
    "groupBy": {
        "Enhancements": ["enhancement", "internal", "feature", "feat"],
        "Bug Fixes": ["bug", "fix"],
        "Documentation": ["docs", "question"],
        "No user affected": ["chore", "refactor", "perf", "test", "style"],
        "CI": ["ci"],
        "Dependency updates": ["dependency", "dependencies"]
    },
    "changelogFilename": "CHANGELOG.md",
    "template": {
        commit: ({ message, url, author, name }) => `- [${message}](${url}) - ${author ? `@${author}` : name}`,
        issue: "- {{labels}} {{name}} [{{text}}]({{url}})",
        label: "[**{{label}}**]",
        noLabel: "closed",
        changelogTitle: "# Changelog\n\n",
        release: "## {{release}} ({{date}})\n{{body}}",
        releaseSeparator: "\n---\n\n",
        group: function (placeholders) {
          var icon = "🙈"
          if(placeholders.heading == 'Enhancements'){
            icon = "🚀"
          } else if(placeholders.heading == 'Bug Fixes'){
            icon = "🐛"
          } else if(placeholders.heading == 'Documentation'){
            icon = "📚"
          } else if(placeholders.heading == 'No user affected'){
            icon = "🙈"
          } else if(placeholders.heading == 'CI'){
            icon = "⚙️"
          } else if(placeholders.heading == 'Dependency updates'){
            icon = "📦"
          }
          return '\n#### ' + icon + ' ' + placeholders.heading + '\n';
        }
    }
}
