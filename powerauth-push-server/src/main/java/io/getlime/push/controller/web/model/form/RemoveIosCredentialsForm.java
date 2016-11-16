package io.getlime.push.controller.web.model.form;

import javax.validation.constraints.NotNull;

/**
 * Form sent when removing iOS / APNs credentials from the application.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class RemoveIosCredentialsForm {

    @NotNull
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override public String toString() {
        return "RemoveIosCredentialsForm{" +
                "id=" + id +
                '}';
    }
}
