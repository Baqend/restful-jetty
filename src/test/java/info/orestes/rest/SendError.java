package info.orestes.rest;

import info.orestes.rest.error.RestException;

/**
 * Created by Florian on 16.07.2015.
 */
public class SendError extends Error {
    public SendError(RestException e) {
        super(e);
    }
}
