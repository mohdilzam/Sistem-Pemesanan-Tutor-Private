import java.util.*;
import java.time.*;
import java.util.regex.*;
import java.util.stream.Collectors;

// ===================== MODEL =========================
class User {
    static int idCounter = 1;
    String userId, name, email, password, role;

    public User(String name, String email, String password, String role) {
        this.userId = "U" + idCounter++;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}

class Message {
    String fromUserId, toUserId, text;
    LocalDateTime timestamp;

    public Message(String fromUserId, String toUserId, String text) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }
}

class Booking {
    static int idCounter = 1;
    String bookingId, studentId, tutorId;
    LocalDateTime dateTime;
    String status = "Pending";
    String paymentStatus = "Unpaid";

    public Booking(String studentId, String tutorId, LocalDateTime dateTime) {
        this.bookingId = "B" + idCounter++;
        this.studentId = studentId;
        this.tutorId = tutorId;
        this.dateTime = dateTime;
    }
}

// ===================== INTERFACES =========================
interface IUserMgt {
    User registerUser(String name, String email, String password, String role);
    User loginUser(String email, String password);
    User getUserById(String userId);
}

interface IMessageMgt {
    Message sendMessage(String fromUserId, String toUserId, String text);
    List<Message> getMessagesSentByUser(String userId);
    List<Message> getMessagesReceivedByUser(String userId);
}

interface IBookingMgt {
    Booking createBooking(String studentId, String tutorId, LocalDateTime dateTime);
    Booking getBookingById(String bookingId);
    List<Booking> getBookingsByUser(String userId);
}

// ===================== IMPLEMENTASI =========================
class SystemApp implements IUserMgt, IMessageMgt, IBookingMgt {
    List<User> users = new ArrayList<>();
    List<Message> messages = new ArrayList<>();
    List<Booking> bookings = new ArrayList<>();

    // Register a new user
    public User registerUser(String name, String email, String password, String role) {
        if (users.stream().anyMatch(u -> u.email.equals(email))) return null;
        if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) return null;
        if (password.length() < 8) return null;
        if (!(role.equals("Student") || role.equals("Tutor"))) return null;

        User user = new User(name, email, password, role);
        users.add(user);
        return user;
    }

    // Login user
    public User loginUser(String email, String password) {
        return users.stream().filter(u -> u.email.equals(email) && u.password.equals(password)).findFirst().orElse(null);
    }

    // Get user by ID
    public User getUserById(String userId) {
        return users.stream().filter(u -> u.userId.equals(userId)).findFirst().orElse(null);
    }

    // Send a message
    public Message sendMessage(String fromUserId, String toUserId, String text) {
        if (fromUserId.equals(toUserId)) return null;
        if (text.isEmpty()) return null;
        if (getUserById(fromUserId) == null || getUserById(toUserId) == null) return null;

        Message msg = new Message(fromUserId, toUserId, text);
        messages.add(msg);
        return msg;
    }

    // Get all messages sent by a user
    public List<Message> getSentMessages(String userId) {
        return messages.stream().filter(m -> m.fromUserId.equals(userId)).collect(Collectors.toList());
    }

    // Get all messages received by a user
    public List<Message> getReceivedMessages(String userId) {
        return messages.stream().filter(m -> m.toUserId.equals(userId)).collect(Collectors.toList());
    }

    // Implement interface methods
    @Override
    public List<Message> getMessagesSentByUser(String userId) {
        return getSentMessages(userId);
    }

    @Override
    public List<Message> getMessagesReceivedByUser(String userId) {
        return getReceivedMessages(userId);
    }

    // Create a booking
    public Booking createBooking(String studentId, String tutorId, LocalDateTime dateTime) {
        if (!dateTime.isAfter(LocalDateTime.now())) return null;
        User student = getUserById(studentId);
        User tutor = getUserById(tutorId);
        if (student == null || !student.role.equals("Student")) return null;
        if (tutor == null || !tutor.role.equals("Tutor")) return null;

        boolean conflict = bookings.stream().anyMatch(b -> b.tutorId.equals(tutorId) && b.dateTime.equals(dateTime) && !b.status.equals("Rejected"));
        if (conflict) return null;

        Booking booking = new Booking(studentId, tutorId, dateTime);
        bookings.add(booking);
        return booking;
    }

    // Get booking by ID
    public Booking getBookingById(String bookingId) {
        return bookings.stream().filter(b -> b.bookingId.equals(bookingId)).findFirst().orElse(null);
    }

    // Get all bookings for a user
    public List<Booking> getBookingsByUser(String userId) {
        return bookings.stream().filter(b -> b.studentId.equals(userId) || b.tutorId.equals(userId)).collect(Collectors.toList());
    }
}

// ===================== MAIN MENU CONSOLE =========================
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SystemApp app = new SystemApp();
        User currentUser = null;

        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Register\n2. Login\n3. Send Message\n4. View Messages\n5. Create Booking\n6. View Bookings\n7. Exit");
            System.out.print("Pilih: ");
            int choice = sc.nextInt(); sc.nextLine();

            switch (choice) {
                case 1: {
                    System.out.print("Nama: "); String name = sc.nextLine();
                    System.out.print("Email: "); String email = sc.nextLine();
                    System.out.print("Password: "); String pass = sc.nextLine();
                    System.out.print("Role (Student/Tutor): "); String role = sc.nextLine();
                    User user = app.registerUser(name, email, pass, role);
                    System.out.println(user != null ? "Berhasil register! ID: " + user.userId : "Gagal register!");
                    break;
                }
                case 2: {
                    System.out.print("Email: "); String email = sc.nextLine();
                    System.out.print("Password: "); String pass = sc.nextLine();
                    currentUser = app.loginUser(email, pass);
                    System.out.println(currentUser != null ? "Login sukses sebagai: " + currentUser.name : "Login gagal");
                    break;
                }
                case 3: {
                    if (currentUser == null) { System.out.println("Login dulu."); break; }
                    System.out.print("Ke User ID: "); String to = sc.nextLine();
                    System.out.print("Pesan: "); String text = sc.nextLine();
                    Message msg = app.sendMessage(currentUser.userId, to, text);
                    System.out.println(msg != null ? "Pesan terkirim" : "Gagal kirim pesan");
                    break;
                }
                case 4: {
                    if (currentUser == null) { System.out.println("Login dulu."); break; }
                    System.out.println("Pesan Dikirim:");
                    app.getMessagesSentByUser(currentUser.userId).forEach(m -> System.out.println("-> " + m.text + " ke " + m.toUserId));
                    System.out.println("Pesan Diterima:");
                    app.getMessagesReceivedByUser(currentUser.userId).forEach(m -> System.out.println("<- " + m.text + " dari " + m.fromUserId));
                    break;
                }
                case 5: {
                    if (currentUser == null || !currentUser.role.equals("Student")) {
                        System.out.println("Login sebagai Student terlebih dahulu."); break;
                    }
                    System.out.print("Tutor ID: "); String tutorId = sc.nextLine();
                    System.out.print("Tanggal Booking (yyyy-MM-ddTHH:mm): "); String dt = sc.nextLine();
                    LocalDateTime dateTime = LocalDateTime.parse(dt);
                    Booking booking = app.createBooking(currentUser.userId, tutorId, dateTime);
                    System.out.println(booking != null ? "Booking dibuat: " + booking.bookingId : "Gagal booking");
                    break;
                }
                case 6: {
                    if (currentUser == null) { System.out.println("Login dulu."); break; }
                    app.getBookingsByUser(currentUser.userId).forEach(b ->
                        System.out.println("[" + b.bookingId + "] " + b.dateTime + " | status: " + b.status));
                    break;
                }
                case 7: System.exit(0); break;
                default: System.out.println("Pilihan tidak valid"); break;
            }
        }
    }
}
