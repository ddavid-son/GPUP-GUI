# <p align="center">G.P.U.P Desktop Application</p>

<p align="center"><img src="https://i.ibb.co/6m4xm80/DD.png" alt="Gpup - logo" border="0"></p>

***

### Table Of Contents

- [Description](#description)
- [How To Use](#how-to-use)
- [Technologies](#technologies)
- [My Info](#my-info)
***


## Description
### G.P.U.P - Generic Platform for Utilizing Processes
GPUP is a task utilizing toll that was created in order to help you save time and resource in executing your tasks, GPUP follows the Open-Close principle and can be extended with various tasks in the future. GPUP was built from the ground up in order to use multi threading to give you the best possible performances.
Tasks can be, for example: compilation task, testing task etc...
***

## How To Use
 The platform enables the user to model a set of dependencies between components(tasks) and handle them efficiently. G.P.U.P capabilities includes:

 - <B>Insights:</B> The platform enables to extract various insights out of the ‘graph’ of tasks such as:
	- routes between nodes to see determine and see the relation between them.
	- circles, helps in finding mutual dependencies and get the specific task that is affected by them
	- transitive dependencies enables the user to see all the tasks that this specific target depends on or all the targets that depends on this task, both directly and transitively.
    <br />

- <B>Serial Sets:</B> The system support setting up serial set meaning it the user want to set a groupe of tasks that aren't dependent on each other but can not run in parallel due to any reason he can set it up in the xml file(as we saw above) and the engine will make sure it will not happen.

- <B>Creating a visual representation of the graph:</b> In the dashboard screen the user have an option to create a visual graph representation to the loaded graph, the photo will be saved as png in the folder the user specifies and there is an option to open it inside the app or in the default image viewer in your machine. this requires the user to have [GraphViz](https://graphviz.org/download/) installed and in path.
	
- <B>Execution:</B> includes few steps:
    - <B>Uploading a graph:</B> the admin needs to upload graph to the system this step will be done by providing the system with XML file that meets the criteria of the schema provided [here](https://github.com/ddavid-son/G.P.U.P-Web/blob/main/testing/xmlSchema.xsd) you can see examples in [here](https://github.com/ddavid-son/G.P.U.P-Web/blob/main/testing/XML1.xml).
    - Creating task can be achieved in 2 ways:
        - <B>From scratch:</B> in the graph dashboard, by clicking the create task button the admin will be asked to choose the type of task he wants to create, then provide information to the system accordingly. the admin can choose whether or not he wants to create the task from the entire graph, specific nodes or all nodes that are effected by his selected nodes(depends on or required for them).
        - <B>From the previous task(incremental):</B> the admin can choose the previous task and duplicate it as is(with all its original nodes) or only with the failed and skipped nodes in order to save time and increase efficiency.
- <B>Task control panel:</B> after the task has been created the user will be greeted with the control panel of the task, there he can control and get information live from the engine, some of the capabilities include:
    - <B>pause/resume the task:</B> the user can change the number of threads executing the task currently.
    - <B>pause/resume:</B> the user can pause and resume as he sees fit during the execution.
    - <B>live feedback:</B> the user would get all the information about every node/sub-task in the graph in a colorful and informative way by seeing the targets moving between the lists of the possible states
    - live feedback will be given in a log-view in the right part of the screen with more detailed information about every target + all the logs will be saved in the file system as well under a folder carrying the name and date of the task creation
    - <B>specific target information:</B> in case the user wants to know even more details about a specific target he can click on it and the system will provide him even more details about it.
    <br />

[Back To The Top](#table-of-contents)
***

#### Technologies

- <B>IntelliJ</B> - While building this app I used this wonderful IDE
- <B>Scene Builder</B> - While Building the UI I have used this tool extensively to design and built the skeleton of the screens.
- <B>JavaFX</B> - JavaFX is the tool I used to implement my UI clients

[Back To The Top](#table-of-contents)
***

## My Info

- GitHub - [See My Work](https://github.com/ddavid-son)
- LinkedIn - [David Davidson](https://www.linkedin.com/in/david-davidson-7067b918a/)

[Back To The Top](#table-of-contents)
***
