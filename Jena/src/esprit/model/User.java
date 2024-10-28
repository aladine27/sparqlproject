package esprit.model;

public class User {
    private String name;
    private int age;
    private String email;
    private int height;
    private String phone;
    private double weight;
    private int heartRate;
    private String bloodPressure;
    private int cholesterol;
    private String diabetesStatus;

    // Default constructor
    public User() {
    }

    // Parameterized constructor
    public User(String name, int age, String email, int height, String phone,
                double weight, int heartRate, String bloodPressure, int cholesterol,
                String diabetesStatus) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.height = height;
        this.phone = phone;
        this.weight = weight;
        this.heartRate = heartRate;
        this.bloodPressure = bloodPressure;
        this.cholesterol = cholesterol;
        this.diabetesStatus = diabetesStatus;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public int getHeight() {
        return height;
    }

    public String getPhone() {
        return phone;
    }

    public double getWeight() {
        return weight;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public int getCholesterol() {
        return cholesterol;
    }

    public String getDiabetesStatus() {
        return diabetesStatus;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public void setCholesterol(int cholesterol) {
        this.cholesterol = cholesterol;
    }

    public void setDiabetesStatus(String diabetesStatus) {
        this.diabetesStatus = diabetesStatus;
    }

    public String getIndividualUri() {
        return "http://example.com/users/" + email; // Adjust as needed
    }
}
