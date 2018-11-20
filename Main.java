/*
jTd -> Java Task program
The program will work in both command line arguments and command line menu, the data will be saved as a json file
Task attributes:
-Creation date  format : dd-MM-yyyy HH:mm
-Content
-Status
-Prority
-Deadline
-Priority (Maybe?)
*/

//TODO: Add sorting by priority
//TODO: A cleaner way to put the data in the json file??

//Todo Find a cleaner way to handle the fileRead if the file is empty (Right now i get a JsonException)
//Todo Make the task's editable
//Todo Make it possible to sort the tasks by date/priority/hybrid
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.json.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    static ArrayList<Task> taskList = new ArrayList<Task>();
    static final String fileName = System.getProperty("user.home") + "/.jTddata";
    static boolean menu = false;
    public static void main(String[] args){
        readFromFile();
        if(args.length > 1){
            addTask(args);
        }
        else if(args.length == 1){
            parseArgs(args[0]);
        }
        else{
            menu = true;
            showMenu();
        }
        writeToFile();

    }


    static void parseArgs(String input){
        switch(input){
            case "-check":
                checkTask();
                break;
            case "-show":
                printTasks(true);
                break;
            case "-log":
                printTasks(false);
                break;
            case "-priority":
                addPriority();
            case "-menu":
                menu = true;
                showMenu();
                break;
            default:
                System.out.println("Bad input");
                System.exit(0);

        }
    }
    static void addTask(String[] task){
        taskList.add(new Task(task));
        System.out.println(taskList.get(taskList.size() - 1));
        writeToFile();
    }
    static void addTask(){
        System.out.println("Insert task description: ");
        Scanner in = new Scanner(System.in);
        String content = in.nextLine();
        taskList.add(new Task(content));
        showMenu();
    }
    static void printTasks(boolean pending){
        if(pending){
            for(Task t : taskList){
                if(t.getStatus() == false){
                    System.out.print(t);
                }
            }
        }
        else{
            for(int i = 0; i < taskList.size(); i++){
                System.out.print("[ " + i + " ]" + taskList.get(i));
            }
        }
    }
    static void addPriority(){
        printTasks(false);
        System.out.println("Choose a task to edit priority: ");
        int input = takeInput(-1, taskList.size() - 1);
        if(input == -1){
            showMenu();
        }
        else{
            int priority = takeInput(0, 3);
            taskList.get(input).setPriority(priority);
        }
        showMenu();
    }
    static void checkTask(){
        printTasks(false);
        System.out.println("Choose a task to check: ");
        int input = takeInput(-1, taskList.size() - 1);
        if(input == -1){
            showMenu();
        }
        else{
            taskList.get(input).check();
        }
        showMenu();
    }
    static void showMenu(){
        if(!menu){
            writeToFile();
        }
        System.out.println("\n---Java Todo---");
        System.out.println("\n[1] Add Task\n[2] Check Task\n[3] Edit priority\n[4] Pending Tasks\n[5] All tasks\n[0] Quit");
        int input = takeInput(0, 5);
        switch (input){
            case 1:
                addTask();
                break;
            case 2:
                checkTask();
                break;
            case 3:
                addPriority();
                break;
            case 4:
                printTasks(true);
                showMenu();
            case 5:
                printTasks(false);
                showMenu();
            case 0:
                System.out.println("Quit");
                writeToFile();
            default: System.out.println("You shouldn't see this!!");
        }

    }
    static int takeInput(int min, int max){
        int input = 0;
        boolean badInput = true;
        Scanner in = new Scanner(System.in);

        System.out.println("Insert input [ " + min + " - " + max + " ] :");
        do{
            try{
                input = in.nextInt();
                if(input >= min && input <= max){
                    badInput = false;
                    return input;
                }
                badInput = true;
                input = -1;
                System.out.println("Bad input");
            }
            catch(InputMismatchException e){
                System.out.println("Bad input");
            }
            in.nextLine();
        }while(badInput);

        System.out.println("Something went wrong!");
        return -1;
    }
    static void writeToFile(){
        JsonArray taskJsonArray = null;
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        for(Task t : taskList){
            objectBuilder.add("Date", t.getStringDate())
                    .add("Content", t.getContent())
                    .add("Priority", t.getPriority())
                    .add("Status", t.getStatus())
                    .add("Completed", t.getStringCheckDate());
            arrayBuilder.add(objectBuilder.build());
        }
        taskJsonArray = arrayBuilder.build();
        try{
            File fl = new File(fileName);
            FileOutputStream out = new FileOutputStream(fl);
            JsonWriter jWriter = Json.createWriter(out);
            jWriter.writeArray(taskJsonArray);
            out.close();
            jWriter.close();
        }
        catch(IOException e){System.err.println(e);}
        System.exit(0);
    }
    static void readFromFile(){
        JsonArray data = null;
        try{
            File fl = new File(fileName);
            fl.createNewFile();
            FileInputStream in = new FileInputStream(fl);
            JsonReader jReader = Json.createReader(in);
            data = jReader.readArray();
            jReader.close();
            in.close();
        }
        catch(Exception e){System.err.println(e);}
        if(data == null){
            System.out.println("No data found");
        }
        else{
            for(int i = 0; i < data.size(); i++) {
                JsonObject temp = data.getJsonObject(i);
                if (temp.getBoolean("Status") == true) {
                    taskList.add(new Task(temp.getString("Content"), temp.getString("Date"), temp.getInt("Priority"), temp.getString("Completed")));
                } else {
                    taskList.add(new Task(temp.getString("Content"), temp.getString("Date"), temp.getInt("Priority")));
                }
            }
        }
    }

}
class Task{
    private String content;
    private String defaultCheckDate = "01/01/1990 12:00:00";
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private LocalDateTime date;
    private boolean status;
    private int priority;
    private LocalDateTime deadline;
    private LocalDateTime checkDate;

    public Task(String[] args){
        content = "";
        for(String s : args){
            content += s + " ";
        }
        date = LocalDateTime.now();
        status = false;
        priority = -1;
    }
    //This is for when the user makes a task
    public Task(String content){
        this.content = content;
        date = LocalDateTime.now();
        status = false;
        priority = -1;
        this.checkDate = LocalDateTime.parse(defaultCheckDate, dateFormat);
    }
    //This is a pending task
    public Task(String content, String date, int priority){
        this.content = content;
        this.date = LocalDateTime.parse(date, dateFormat);
        this.status = false;
        this.priority = priority;
        this.checkDate = LocalDateTime.parse(defaultCheckDate, dateFormat);
    }
    //This is a completed task
    public Task(String content, String date, int priority, String checkDate){
        this.content = content;
        this.date = LocalDateTime.parse(date, dateFormat);
        this.status = true;
        this.priority = priority;
        this.checkDate = LocalDateTime.parse(checkDate, dateFormat);
    }
    public String getStringDate(){return date.format(dateFormat);}
    public LocalDateTime getDate(){return date;}
    public String getContent(){return content;}
    public int getPriority(){return priority;}
    public boolean getStatus(){return status;}
    public String getStringCheckDate(){
        if(status) {
            return checkDate.format(dateFormat);
        }
        return defaultCheckDate;
    }

    public void setPriority(int priority){
        assert priority <= 2 && priority >= 0;
        this.priority = priority;
    }

    public void check(){
        status = !status;
        if(status){
            checkDate = LocalDateTime.now();
        }
        else{
            checkDate = LocalDateTime.parse(defaultCheckDate, dateFormat);

        }
    }
    public String statusFormat(){
        if(status){
            return "COMPLETED ON " + checkDate.format(dateFormat);
        }
        return "PENDING";
    }
    public String priorityFormat(){
        switch (priority){
            case 0:
                return "LOW";
            case 1:
                return "MEDIUM";
            case 2:
                return "HIGH";
            default: return "NONE";

        }
    }

    public String toString(){
        return "[Date : " + date.format(dateFormat) + " ] [Priority : " + priorityFormat() + " ] [Status : " + statusFormat() + " ] [Content : " + content + " ]\n";
    }
}
