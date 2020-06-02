package com.projeto.eventplay.Modelo;

import java.util.ArrayList;
import java.util.List;

public class Categoria {

    private String id;
    private List<Evento> eventos;

    public Categoria() {
        eventos = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Evento> getEventos() {
        return eventos;
    }

    public void setEventos(List<Evento> eventos) {
        this.eventos = eventos;
    }
}
