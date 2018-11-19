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
//TODO 1: Find a way to add the completed date to the json file (Maybe choose an arbitrary date for a pending task?)
//TODO 2: Redo the checkTask method to only show the indexes of the pending tasks

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
    public static void main(String[] args){
        readFromFile();
        if(args.length > 1){
            addTask(args);
        }
        else if(args.length == 1){
            parseArgs(args[0]);
        }
        else{
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
                System.out.println("Show");
                break;
            case "-log":
                allTasks();
                break;
            case "-menu":
                showMenu();
                break;
            default:
                System.out.println("Bad iiiinput");
                System.exit(0);

        }
    }
    static void addTask(String[] task){
        taskList.add(new Task(task));
        System.out.println(taskList.get(taskList.size() - 1));
        writeToFile();
    }
    static void addTask(){
        Scanner in = new Scanner(System.in);
        String content = in.nextLine();
        taskList.add(new Task(content));
        in.close();
        showMenu();
    }
    static void allTasks(){
        for(Task t : taskList){
            System.out.print(t);
        }
        showMenu();
    }
    static void checkTask(){
        for(int i = 0; i < taskList.size(); i++){
            if(taskList.get(i).getStatus() == false) {
                System.out.println("< " + i + " >");
                System.out.println(taskList.get(i));
            }
        }
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
        System.out.println("\n---Java Todo---");
        System.out.println("\n[1] Add Task\n[2] Check Task\n[3] Pending Tasks\n[4] All Tasks\n[0] Quit");
        int input = takeInput(0, 4);
        switch (input){
            case 1:
                addTask();
                break;
            case 2:
                checkTask();
                break;
            case 3:
                System.out.println("Pending tasks");
                break;
            case 4:
                allTasks();
                break;
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
                    .add("Status", t.getStatus());
            if(t.getStatus() == true){
                objectBuilder.add("Completed", t.getStringCheckDate());
            }
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
            FileInputStream in = new FileInputStream(fl);
            JsonReader jReader = Json.createReader(in);
            data = jReader.readArray();
            jReader.close();
            in.close();
        }
        catch(Exception e){System.err.println(e);}

        for(int i = 0; i < data.size(); i++){
            JsonObject temp = data.getJsonObject(i);
            taskList.add(new Task(temp.getString("Content"), temp.getString("Date"), temp.getBoolean("Status"), temp.getInt("Priority")));
        }
    }

}
class Task{
    private String content;
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
    public Task(String content){
        this.content = content;
        date = LocalDateTime.now();
        status = false;
        priority = -1;
    }
    public Task(String content, String date, boolean status, int priority){
        this.content = content;
        this.date = LocalDateTime.parse(date, dateFormat);
        this.status = status;
        this.priority = priority;
    }
    public String getStringDate(){return date.format(dateFormat);}
    public LocalDateTime getDate(){return date;}
    public String getContent(){return content;}
    public int getPriority(){return priority;}
    public boolean getStatus(){return status;}
    public String getStringCheckDate(){return checkDate.format(dateFormat);}

    public void check(){
        status = true;
        checkDate = LocalDateTime.now();
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
        return "\n[Date : " + date.format(dateFormat) + " ] [Priority : " + priorityFormat() + " ] [Status : " + statusFormat() + " ] [Content : " + content + " ]";
    }
}
