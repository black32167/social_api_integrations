# Birdview
## Abstract
Birdview is a command-line tool enabling you to represent your work context in form of 
list of recently done (or planned) activities.

The tool also can try to group related activities together using 
[TF-IDF](https://en.wikipedia.org/wiki/Tf–idf) - based approach to determine degree of relatedness between activities.

There are following types of activity item types supported:
 - jira tickets
 - trello cards
 - Github pull requests
 
## Usage
### Configuration
To use the tool you need to configure access to sources of your features in form of JSON file
('bv.json')

```json
{
  "jira" : {
    "baseUrl" : "https://your-jira-domain.atlassian.net",
    "user" : "your-email@your-company.com",
    "token" : "your-personal-jira-token"
  },
  "trello" : {
    "baseUrl" : "https://api.trello.com",
    "key" : "your-personal-trello-key",
    "token" : "your-personal-trello-token"
  },
  "github" : {
    "baseUrl" : "https://api.github.com",
    "user" : "your-email@your-company.com",
    "token" : "your-personal-github-token"
  }
}

```
### Running

```shell script
$ docker run --rm -v "path/to/config/folder":/config black32167/birdview progress

2020-05-05 - Document my recent work : https://trello.com/c/xxxxxx/82-document-recent-work
2020-05-05 - Fixed production issue : https://github.com/MyOrg/repo/pull/14556
[Infrastructure Improvements]
    2020-05-05 - Infrastructure Improvements: Provision standby RDS instance: https://your-jira-domain.atlassian.net/browse/XXXX-123
    2020-05-05 - Infrastructure Improvements: Create alerts for EC2 low disk space : https://your-jira-domain.atlassian.net/browse/XXXX-321
```