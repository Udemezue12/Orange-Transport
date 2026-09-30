package com.astrotech.transport.mappers;

import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.dto.request.TerminalRequest;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.entities.*;

public class TerminalMapper {
    public static Terminal createTerminal(TerminalRequest request, User user){
        var state = TrimWhiteSpace.trimWhiteSpace(request.state());
        var city = TrimWhiteSpace.trimWhiteSpace(request.city());
        var name = TrimWhiteSpace.trimWhiteSpace(request.terminalName());
        return Terminal.builder()
                .state(state)
                .name(name)
                .city(city)
                .address(request.address())
                .terminalSupervisor(user)
                .build();

    }
    public static TerminalResponse toResponse(Terminal terminal){
        var userResponse = UserMapper.response(terminal.getTerminalSupervisor());
        return new TerminalResponse(
                simpleResponse(terminal),
                userResponse
        );
    }
    public static SimpleTerminalResponse simpleResponse(Terminal terminal){

        return new SimpleTerminalResponse(
                terminal.getId(),
                terminal.getName(),
                terminal.getState(),
                terminal.getCity(),
                terminal.getAddress()
        );
    }

}
