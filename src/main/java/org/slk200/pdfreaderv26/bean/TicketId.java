package org.slk200.pdfreaderv26.bean;

/**
 * 工单ID
 */
public class TicketId {
    private String ticket_id;
    private String ticket_state;
    private String ticket_time;

    public TicketId(String ticket_id, String ticket_state, String ticket_time) {
        this.ticket_id = ticket_id;
        this.ticket_state = ticket_state;
        this.ticket_time = ticket_time;
    }

    public String getTicket_time() {
        return ticket_time;
    }

    public void setTicket_time(String ticket_time) {
        this.ticket_time = ticket_time;
    }

    public String getTicketId() {
        return ticket_id;
    }

    public void setTicketId(String orderId) {
        this.ticket_id = orderId;
    }

    public String getTicket_state() {
        return ticket_state;
    }

    public void setTicket_state(String ticket_state) {
        this.ticket_state = ticket_state;
    }

    @Override
    public String toString() {
        return "TicketId{" +
                "ticket_id='" + ticket_id + '\'' +
                ", ticket_state=" + ticket_state +
                ", ticket_time='" + ticket_time + '\'' +
                '}';
    }
}
