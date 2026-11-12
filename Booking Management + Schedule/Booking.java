import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Booking{
    private String bookingID;
    private String userID;
    private String roomID;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;

    public Booking(String bookingID, String userID, String roomID, LocalDate date, LocalTime startTime, LocalTime endTime, String purpose) {
        this.bookingID = bookingID;
        this.userID = userID;
        this.roomID = roomID;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
    }

    public void getbooking(){
        System.out.println(
            "Booking ID: " + bookingID 
            + "\nUser ID: " + userID
            + "\nRoom ID: " + roomID
            + "\nDate of booking: " + date
            + "\nStart Time: " + startTime
            + "\nEnd Time: " + endTime
            + "\nPurpose of booking: " + purpose
        );
    }

    public boolean hasConflict(String roomId, LocalDate date, LocalTime start, LocalTime end){
        if (roomId.equals(this.roomID) && date.equals(this.date) 
            && start.isBefore(endTime) && this.startTime.isBefore(end)){
                return true;
            }
        return false;
    }

    //Getters and Setters
    public String getBookingId(){ 
        return bookingID;
    }
    public String getUserId(){ 
        return userID;
    }
    public String getRoomId(){ 
        return roomID;
    }
    public LocalDate getDate(){ 
        return date;
    }
    public LocalTime getStartTime(){ 
        return startTime;
    }
    public LocalTime getEndTime(){ 
        return endTime;
    }
    public String getPurpose(){ 
        return purpose;
    }

    public void setRoomId(String roomID){ 
        this.roomID = roomID;
    }
    public void setDate(LocalDate date){ 
        this.date = date;
    }
    public void setStartTime(LocalTime startTime){ 
        this.startTime = startTime;
    }
    public void setEndTime(LocalTime endTime){ 
        this.endTime = endTime;
    }
    public void setPurpose(String purpose){ 
        this.purpose = purpose;
    }
}
