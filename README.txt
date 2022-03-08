# <p align="center">G.P.U.P Web Client-Server Application</p>

<p align="center"><img src="https://i.ibb.co/6m4xm80/DD.png" alt="Gpup - logo" border="0"></p>

***

### Table Of Contents

- [Description](#description)
- [How To Use](#how-to-use)
- [How To Use Admin](#admin-client)
- [How To Use Worker](#worker-client)
- [My Info](#my-info)
***


## Description
### G.P.U.P - Generic Platform for Utilizing Processes
GPUP follows the Open-Close principle and can be extended with various tasks in the future.
Tasks can be, for example: compilation task, testing task etc...
The platform architecture enables distributed (remote) workers to connect to it and execute work on their private resources.
***

## How To Use
### Admin Client:

 The platform enables the Admin to model set of dependencies between components(tasks) and handle them efficiently. G.P.U.P capabilities includes:

 - <B>Insights:</B> The platform (Admin app) enables to extract various insights out of the ‘graph’ of tasks such as:
	- routes between nodes to see determine and see the relation between them.
	- circles, helps in finding mutual dependencies and get the specific task that is affected by them
	- transitive dependencies enables the user to see all the tasks that this specific target depends on or all the targets that depends on this task, both directly and transitively.
    <br />
	
- <B>Execution</B> includes few steps: 
    - <B>Uploading a graph:</B> the admin needs to upload graph to the system(unless he wants to use an existing one from other admins) this step will be done by providing the system with XML file that meets the criteria of the schema provided [here](https://github.com/ddavid-son/G.P.U.P-Web/blob/main/testing/xmlSchema.xsd) you can see examples in [here](https://github.com/ddavid-son/G.P.U.P-Web/blob/main/testing/XML1.xml).
    - <B>Creating task can be achieved in 2 ways:</B> 
        - <B>From existing graph:</B> in the graph dashboard, by clicking the create task button the admin will be asked to choose the type of task he wants to create, then provide information to the system accordingly. the admin can choose whether or not he wants to create the task from the entire graph, specific nodes or all nodes that are effected by his selected nodes(depends on or required for them).
        - <B>From existing task:</B> the admin can choose and already existing *finished* task and duplicate it as is(with all its original nodes) or only with the failed and skipped nodes in order to save time. this can be accomplished in the task control center screen.
    - <B>Activating the task:</B> in this stage the task is live on the server but no one can start working on the nodes that are ready to be worked on(i.e all its required nodes has been finished successfully), the admin needs to got the the control panel of the task and activate it. Once activated the task will provide the listed worker with the available nodes.
    - *It is worth noting that in the control panel of the task the admin can pause and resume the task as he sees right as well as abort it entirely.*
    <br />
- <B>Feedback:</B> After the task has been created every admin can see its exact status as well as the status of each and every node in the control panel. In addition every node is clickable and when clicked will provide extended information about her status and state.

[Back To The Top](#table-of-contents)
***



### Worker Client
The platform enables the Worker to enroll into tasks as he sees fit, and get work done on them using his own computing force(as much as he desire to contribute) and get paid accordingly the Worker app capabilities  includes:
- <B>Register resource:</B> the worker needs to supply the number of threads that he grants the system to make use of - limited to 5.
- <B>Enrollment:</B> The worker can choose any task that is live on the system and ask to join her(as long as she is not finished or aborted) this takes place in the dashboard screen. of course the worker can remove himself from any task that he previously enrolled to at any time.
- <B>Execution:</B> After enrolling to a task the worker asks the server automatically to get him job to do meaning that he calculates how many threads he have that are not doing anything asks the server for this amount of work and gets nodes from the server accordingly. *it is worth noting that the fetching of work is done from the perspective of the whole app and not from each task in order to prevent meaningless calls and overflowing the server with inefficient work*
- <B>Info about tasks:</B> The worker can see a table in the dashboard screen with all the tasks loaded in the system with all the relevant info, the table is updated every 2 seconds so it is always up to date.
- <B>Info about work done:</B> The worker can access the info center, there he will be granted by two(auto updating tables):
    - The first table shows all the information about all the work that he has done, the payment that he got and more.
    - The second table shows data about all the tasks that he is enrolled to with data such as it status state and more.



<B>*The execution of nodes(targets/work) is done in parallel using multi-threading implementation to maximize efficiency of processing*</B>

[Back To The Top](#table-of-contents)
***

#### Technologies

- <B>IntelliJ</B> - While building this app I used this wonderful IDE
- <B>Scene Builder</B> - While Building the UI I have used this tool extensively to design and built the skeleton of the screens.
- <B>Apache Tomcat</B> - this application runs on apache tomcat server.
- <B>GSON</B> - GSON helped allot in sending, receiving and converting objects from java to json and vice versa.
- <B>OKHttpClient</B> - Is the tool I choose to use as my http client
- <B>JavaFX</B> - JavaFX is the tool I used to implement my UI clients

[Back To The Top](#table-of-contents)
***

#### Installation
[Back To The Top](#table-of-contents)
***

## My Info

- GitHub - [See My Work](https://github.com/ddavid-son)
- LinkedIn - [David Davidson](https://www.linkedin.com/in/david-davidson-7067b918a/)

[Back To The Top](#table-of-contents)
***
