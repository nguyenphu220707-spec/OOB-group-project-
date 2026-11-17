package hk.edu.polyu.comp.comp2021.crobs.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BookingManager{
    private RoomManager roomManager;
    private List<Booking> bookingList;
    private int idCount = 1;

    public BookingManager(){
        this.bookingList = new ArrayList<Booking>();
    }

    public BookingManager(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    // Checklist #5: Connect BookingManager to RoomManager
    public void setRoomManager(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    // Checklist #5: Call roomManager.roomExists(roomId)
    public boolean roomExists(String roomId) {
        if (roomId == null || roomManager == null) {
            return false;
        }
        return roomManager.roomExists(roomId);
    }

    // Checklist #3 (REQ3 Helper): Remove all bookings for a deleted user
    public void cancelBookingsByUser(String userId) {
        if (userId == null) {
            return;
        }
        for (int i = bookingList.size() - 1; i >= 0; i--) {
            if (userId.equals(bookingList.get(i).getUserId())) {
                bookingList.remove(i);
            }
        }
    }

    // Checklist #4 (REQ9 Helper A): Return all bookings
    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookingList);
    }

    // Checklist #4 (REQ9 Helper B): Cancel future bookings for a deleted room
    public void cancelFutureBookingsByRoom(String roomId) {
        if (roomId == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        for (int i = bookingList.size() - 1; i >= 0; i--) {
            Booking booking = bookingList.get(i);
            if (roomId.equals(booking.getRoomId())) {
                boolean isFutureDate = booking.getDate().isAfter(today);
                boolean isLaterToday = booking.getDate().isEqual(today) && booking.getStartTime().isAfter(now);
                if (isFutureDate || isLaterToday) {
                    bookingList.remove(i);
                }
            }
        }
    }
    
    //REQ10
    public String createBooking(String userId, String roomId, LocalDate date, LocalTime startTime, LocalTime endTime, String purpose){
        if (userId == null || userId.trim().equals("")) {
            return "Error: You must be logged in to make a booking.";
        }
        if (date == null || startTime == null || endTime == null || endTime.isBefore(startTime) || startTime.equals(endTime)){
            return "Error: Start time must be before end time.";
        }
        if (purpose == null || purpose.trim().equals("")) {
            return "Error: hk.edu.polyu.comp.comp2021.crobs.model.Booking purpose is empty.";
        }
        if (roomId == null || !roomExists(roomId)) { //Need roomExists function
           return "Error: RoomID " + roomId + " does not exist.";
        }

        for (Booking booking: bookingList){
            if (booking.hasConflict(roomId, date, startTime, endTime)){
                return "Error: hk.edu.polyu.comp.comp2021.crobs.model.Booking conflict. The room is already booked during this time slot.";
            }
        }

        String bookingId = String.format("B%03d", idCount++); //Format: 'B' + 3 digit number (has '0' paddings in front if idCount < 100)
        Booking newBooking = new Booking(bookingId, userId, roomId, date, startTime, endTime, purpose.trim());
        this.bookingList.add(newBooking);

        return "hk.edu.polyu.comp.comp2021.crobs.model.Booking created. ID: " + bookingId;
    }

    //REQ11
    public String modifyBooking(String userId, String bookingId, String roomId, LocalDate date, LocalTime startTime, LocalTime endTime, String purpose){
        if (userId == null || userId.trim().equals("")) {
            return "Error: You must be logged in to modify a booking.";
        }
        if (bookingId == null || bookingId.trim().equals("")) {
            return "Error: hk.edu.polyu.comp.comp2021.crobs.model.Booking ID is empty.";
        }
        if (date == null || startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            return "Error: Start time must be before end time.";
        }
        if (purpose == null || purpose.trim().equals("")) {
            return "Error: hk.edu.polyu.comp.comp2021.crobs.model.Booking purpose is empty.";
        }
        if (roomId == null || !roomExists(roomId)) { //Need roomExists function
           return "Error: RoomID " + roomId + " does not exist.";
        }

        //Find target booking in BookingList
        Booking targetBooking = null;
        for (Booking booking : bookingList) {
            if (booking.getBookingId().equals(bookingId)) {
                targetBooking = booking;
                break;
            }
        }

        if (targetBooking == null) {
            return "Error: hk.edu.polyu.comp.comp2021.crobs.model.Booking ID: " + bookingId + " does not exist.";
        }

        //Verify userID
        if (!targetBooking.getUserId().equals(userId)) {
            return "Error: You can only modify your own bookings.";
        }

        //Check for time conflicts with other bookings
        for (Booking booking : bookingList) {
            if (!booking.getBookingId().equals(bookingId) &&
                 booking.hasConflict(roomId, date, startTime, endTime)) {
                return "Error: Time conflict with existing bookingID: " + booking.getBookingId() + ".";
            }
        }

        //All checks passed
        targetBooking.setRoomId(roomId);
        targetBooking.setDate(date);
        targetBooking.setStartTime(startTime);
        targetBooking.setEndTime(endTime);
        targetBooking.setPurpose(purpose.trim());

        return "Success: hk.edu.polyu.comp.comp2021.crobs.model.Booking " + bookingId + " has been modified.";
    }

    //REQ12
    public String deleteBooking(String userId, String bookingId){
        if (userId == null || userId.trim().equals("")) {
            return "Error: You must be logged in to delete a booking.";
        }
        if (bookingId == null || bookingId.trim().equals("")) {
            return "Error: hk.edu.polyu.comp.comp2021.crobs.model.Booking ID cannot be empty.";
        }

        Booking targetBooking = null;
        for (Booking booking : bookingList) {
            if (booking.getBookingId().equals(bookingId)) {
                targetBooking = booking;
                break;
            }
        }

        if (targetBooking == null) {
            return "Error: hk.edu.polyu.comp.comp2021.crobs.model.Booking ID: " + bookingId + " does not exist.";
        }

        if (!targetBooking.getUserId().equals(userId)) {
            return "Error: You can only delete your own bookings.";
        }

        this.bookingList.remove(targetBooking);

        return "Success: BookingID: " + bookingId + " has been deleted.";
    }

    //REQ 17 (helper method)
    public List<Booking> listBookings(String userId){
        if (userId == null || userId.trim().equals("")) {
            return new ArrayList<Booking>();
        }

        List<Booking> userBookings = new ArrayList<Booking>();
        for (Booking booking: bookingList){
            if (booking.getUserId().equals(userId)){
                userBookings.add(booking);
            }
        }

        userBookings.sort(
            java.util.Comparator.comparing(Booking::getDate).thenComparing(Booking::getStartTime)
        );

        return userBookings;
    }

    // REQ 17 (toString method)
    public String getListBookingsString(String userId) {
        if (userId == null || userId.trim().equals("")) {
            return "Error: You must be logged in to list bookings.";
        }

        List<Booking> userBookings = listBookings(userId);
        if (userBookings.isEmpty()) {
            return "No bookings found for user " + userId + ".";
        }

        StringBuilder display = new StringBuilder();
        for (Booking booking : userBookings) {
            display.append("hk.edu.polyu.comp.comp2021.crobs.model.Booking ID: ").append(booking.getBookingId())
                   .append(", hk.edu.polyu.comp.comp2021.crobs.model.Room ID: ").append(booking.getRoomId())
                   .append(", Date: ").append(booking.getDate())
                   .append(", Time: ").append(booking.getStartTime()).append(" - ").append(booking.getEndTime())
                   .append(", Purpose: ").append(booking.getPurpose())
                   .append("\n\n");
        }

        return display.toString().trim();
    }

    //REQ 14 (helper method)
    public List<Booking> getWeeklySchedule(String roomId, LocalDate weekStartDate) {
        List<Booking> weeklyBookings = new ArrayList<Booking>();
        if (roomId == null || weekStartDate == null) {
            return weeklyBookings;
        }

        LocalDate weekEndDate = weekStartDate.plusDays(6); //7 days in week

        for (Booking booking : bookingList) {
            LocalDate bookingDate = booking.getDate();
            if (booking.getRoomId().equals(roomId) 
                    && !bookingDate.isBefore(weekStartDate) 
                    && !bookingDate.isAfter(weekEndDate)) {
                weeklyBookings.add(booking);
            }
        }

        weeklyBookings.sort(
            java.util.Comparator.comparing(Booking::getDate).thenComparing(Booking::getStartTime)
        );

        return weeklyBookings;
    }

    //REQ 14 (toString method)
    public String getWeeklyScheduleString(String userId, boolean isAdmin, String roomId, LocalDate weekStartDate) {
        if (userId == null || userId.trim().equals("")) {
            return "Error: You must be logged in to view a room schedule.";
        }
        if (roomId == null || !roomExists(roomId)) {
            return "Error: RoomID " + roomId + " does not exist.";
        }
        if (weekStartDate == null) {
            return "Error: the week's start date is null.";
        }

        List<Booking> schedule = getWeeklySchedule(roomId, weekStartDate);
        if (schedule.isEmpty()) {
            return "No bookings found for room " + roomId + " from " + weekStartDate + " - " + weekStartDate.plusDays(6) + ".";
        }

        StringBuilder display = new StringBuilder();
        String displayedUsers;
        for (Booking booking : schedule) {
            //Permission access check
            if (isAdmin || booking.getUserId().equals(userId)){
                displayedUsers = booking.getUserId();
            }
            else{
                displayedUsers = "N/A";
            }

            display.append("hk.edu.polyu.comp.comp2021.crobs.model.Booking ID: ").append(booking.getBookingId())
              .append(", Date: ").append(booking.getDate())
              .append(", Time: ").append(booking.getStartTime()).append(" - ").append(booking.getEndTime())
              .append(", User ID: ").append(displayedUsers)
              .append(", Purpose: ").append(booking.getPurpose())
              .append("\n\n");
        }

        return display.toString().trim();
    }
}
