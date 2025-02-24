module org.sinbelisk.graphicftp {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.sinbelisk.graphicftp to javafx.fxml;
    exports org.sinbelisk.graphicftp;
}