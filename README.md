# T7TA Team 7 Transmission App - Thesis Product
This application is a requirement for our thesis project. The checklist of all things to be completed will be on the Trello workspace below.
The workflow we are going to follow is a sort of scrum where the application is broken down into smaller modules for easy monitoring and management.


## Trello Workspace Link
 https://trello.com/invite/b/7BUrbMR7/53828b7688e59364ac8deadca06fcce2/ema-by-t7
 
 ![trello-board-invite-qr-code](https://user-images.githubusercontent.com/54576359/118435185-01f39600-b711-11eb-8c8b-a23b6df56192.png)
 
## How to download this git to your computer

First, copy the link of this git repository from the dropwdown button "Code". After copying, open up git bash and execute the following commands.
```sh
cd <directory you want to save the repository>
git clone <link of the repository.git>
git remote add origin <link of the repository.git>
```

## How to pull changes from this repository

Before you can make changes on the application, of course you have to make sure that you have the latest files from the repository. So you have to PULL files from this repository. The following commands should be executed.
```sh
cd <directory you want to save the repository>
git pull origin master
```
If you have any questions about pulling, just message sa gc or refer to the Part 2 of the Tutorial I uploaded on youtube.

## How to push changes to this repository

PLEASE DO NOT FORGET TO PUSH USING ANOTHER BRANCH. DO NOT PUSH TO MASTER. So when you make changes to the application, let's say add a module, then you have to make a new branch in your computer (can be any name, much better if the branch name is somethuing specific).
```sh
cd <directory of the repository>
git branch <name of new branch>
git checkout <name of new branch created>
git add --a
git commit -m "vX.X Brief Description of Commit/Changes"
git push origin <name of branch created>
```

## Youtube Tutorial Links
### Part 1
[![Part 1](https://i9.ytimg.com/vi_webp/lqgabiyLo-A/mqdefault.webp?time=1621229100000&sqp=CKz8h4UG&rs=AOn4CLCFj9miy6ddDBC4-IlmFvIKKd887g)](https://youtu.be/lqgabiyLo-A)
### Part 2
[![Part 2](https://i9.ytimg.com/vi_webp/dgV-xEjHWC8/mqdefault.webp?time=1621229100000&sqp=CKz8h4UG&rs=AOn4CLAJPOYAiGfihaMoQ7YTGOf1VFO3Gw)](https://youtu.be/dgV-xEjHWC8)
