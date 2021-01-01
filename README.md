# JavaOPSM ( "on-the-fly" Process Servicing and Management )
This Project will be my first take on Process-Module based Programming in Java.

The end-point of this project is to have a process manager with "hot-swappable" process code bases, which means changing a line in a code base of a process can be instantly included into a running system. Also InterProcess Communication between the Processes of course.

## Questions

### Who is creating this ?
That is me, my name is Tino Geißler and I'm a currently-studying Programmer in the field of applied computer science.
This project is a learning experience for me and others who are interessted. 
I do this in my free time, so don't expect any "update-scetchules" or any other stuff. What I will do tho is testing my code and write documentation. If anyone wants to write tests for it, go ahead.

### For how is this ?
It's for People who either want to just use a out-of-the-box Process Manager for their own tools/scripts/modules/and other or for others who like to use open suorce, fully tested code for their own projects and don't want to write stuff like this themself.

### Why java tho? Why not use C on a System level, or C++, or ...
I have the belive that Java is a very consistent and direct programming-language. Also nearly everyone I know can read Java code perfectly. Also Java is running itself in a VM, wich means the code will be running on most if not all devices the same way. Also I don't know C well enough. 

### GNU General Public License ? 
YES! This code base / documantation is and will stay under the GNU General Public License, therefor you can copy the code and use it or write clones with the same structure.

### What is the main point of this project ? InterProcess Communication or Module Management
That is a good question. I kinda started this Project for IPC but you need a reliable way to manage those Processes (modules). So I will first implement the Process(Module) Management and then create the IPC ontop of it.

### Why even do this ? Isn't it just easier to implement communication between Processes if needed manually since every process does have it's own needs of reliable usage ?
Implementing a way to do Interprocess Communication between Process for every single process manually can be done. My Goal is to have a reliable construct for instant usage or even just for guidance for other people. Also I'm just interessted in this Project.  

### Why not just do Thread-based Programming, wouldn't that be just easier?
...probably

### So what's the Plan? 

The base construction can be archived by just let the system recompile the given modules, which is quite easy to implement/already partially implemented. 

The hard part is letting the Processes talking which each other and finding a standard for all needed process structures that could be archived. 

There are a couple of ways to implement a interprocess communication:

 - over file communication ( no good way found yet )
 - over Database communication ( extra seetups needed )
 - over Socket communication ( easyiest and most reliable communication basis for now, but has it's security downsites )
 - over System InputOutputStream communication ( not really reliable )
 - one time communcation (over the parameter list)
