package me.pengu.ventis.connection.implementation.socket;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Server {

    private String address;
    private int port;

}