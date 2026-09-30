package com.astrotech.transport.service;


import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.response.AssignAgentConversationResponse;
import com.astrotech.transport.dto.response.SimpleChatConversationResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.entities.ChatConversation;
import com.astrotech.transport.enums.*;
import com.astrotech.transport.exceptions.*;
import com.astrotech.transport.mappers.ChatConversationMapper;
import com.astrotech.transport.repositories.ChatConversationRepository;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatConversationService {
    private final ChatConversationRepository conversationRepository;
    private final UserService userService;
    private final ProfileService profileService;
    private final AssignWorkerService assignWorkerService;
    private final GetCurrentUser getCurrentUser;

    @Transactional
    @RoleRequired({UserRole.PASSENGER, UserRole.DRIVER, UserRole.VEHICLE_LOADER})
    @CacheEvict(value = "conversation-lists", allEntries = true)
    public SimpleChatConversationResponse createChatConversation(UUID userId) {
        var passenger = userService.getAuthorizedUser(userId);
        var convoMapper = ChatConversationMapper.createConvo(passenger);
        var savedConvo = conversationRepository.save(convoMapper);
        return ChatConversationMapper.simpleChatConversation(savedConvo);
    }

    @Transactional
    @RoleRequired({UserRole.ADMIN,
            UserRole.TERMINAL_SUPERVISOR})
    @CacheEvict(
            cacheNames = "chat-participants",
            key = "#convoId + ':' + #userId"
    )
    public AssignAgentConversationResponse assignAgent(UUID userId, UUID convoId) {
        Objects.requireNonNull(userId, "User Id is required");
        var convo = findConvoById(convoId);
        var currentUserId = getCurrentUser.getCurrentUserIdAndRole().userId();
        if (convo.getStatus() == ConversationStatus.CLOSED) {
            throw new BadRequestException("This conversation has been closed");
        }
        var profile = profileService.findCustomerAgents(userId, UserStatus.ACTIVE, UserRole.CUSTOMER_SERVICE_AGENT);
        if (profile.getIdentityDocument().getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new BadRequestException("Profile is yet to be approved by the management");
        }
        var agent = profile.getUser();

        convo.setAgent(agent);
        convo.setStatus(ConversationStatus.ASSIGNED);
        convo.setUpdatedAt(Instant.now());
        var savedConvo = conversationRepository.save(convo);
        assignWorkerService.assignWorker(agent.getId(), currentUserId, AssignedServiceType.CONVERSATION_CUSTOMER_AGENT, savedConvo.getId());
        return ChatConversationMapper.assignAgentConversationResponse(savedConvo);
    }

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "conversation-lists",
                            allEntries = true
                    ),
                    @CacheEvict(value = "")
            }
    )
    @RoleRequired({UserRole.ADMIN,
            UserRole.TERMINAL_SUPERVISOR
    })
    public SimpleChatConversationResponse closeConvo(UUID convoId) {

        var convo = findConvoById(convoId);
        if (convo.getStatus() == ConversationStatus.CLOSED) {
            return ChatConversationMapper.simpleChatConversation(convo);
        }

        convo.setStatus(ConversationStatus.CLOSED);
        convo.setUpdatedAt(Instant.now());
        var savedConvo = conversationRepository.save(convo);
        assignWorkerService.updateAssignStatus(convo.getId(), AssignStatus.COMPLETED);
        return ChatConversationMapper.simpleChatConversation(savedConvo);
    }


    @RoleRequired(
            {UserRole.ADMIN,
                    UserRole.PASSENGER
            }
    )
    @CustomCacheable(
            value = "conversation-lists",
            key = "'userConversations' + #userId + 'all-p' + #page + '-s' + #size",
            ttl = 180,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleChatConversationResponse> findConvosByPassenger(UUID userId, int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, "createdAt", true, ChatConversation
                .class, true);

        var content = conversationRepository.findAllByPassengerId(userId, pageable);
        return getSimpleChatConversationResponseSliceResponse(content);


    }

    @RoleRequired(
            {UserRole.ADMIN,
                    UserRole.PASSENGER
            }
    )
    @CustomCacheable(
            value = "conversation-lists",
            key = "'all-p' + #page + '-s' + #size",
            ttl = 180,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleChatConversationResponse> findAllConversations(int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, "createdAt", true, ChatConversation
                .class, true);

        var content = conversationRepository.findAllBy(pageable);
        return getSimpleChatConversationResponseSliceResponse(content);


    }

    public ChatConversation findConvoById(UUID convoId) {
        return conversationRepository.findById(convoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conversation not found"
                        )
                );
    }

    private static @NonNull SliceResponse<SimpleChatConversationResponse> getSimpleChatConversationResponseSliceResponse(Slice<ChatConversation> result) {
        var content = result.getContent()
                .stream()
                .map(ChatConversationMapper::simpleChatConversation)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }


}
