package com.webapp.Tracker_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating HR notes on an application
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationNotesRequest {
    
    private String hrNotes;  // HR notes/comments on the application
}
