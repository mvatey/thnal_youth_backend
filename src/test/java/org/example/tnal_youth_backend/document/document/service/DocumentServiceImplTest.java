package org.example.tnal_youth_backend.document.document.service;

import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.document.document.dto.request.DocumentRequest;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.document.document.mapper.DocumentMapper;
import org.example.tnal_youth_backend.document.document.repository.DocumentRepository;
import org.example.tnal_youth_backend.document.document.service.impl.DocumentServiceImpl;
import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.example.tnal_youth_backend.document.type.repository.DocumentTypeRepository;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock DocumentRepository documentRepository;
    @Mock DocumentTypeRepository documentTypeRepository;
    @Mock FileRepository fileRepository;
    @Mock BranchRepository branchRepository;
    @Mock MemberRepository memberRepository;
    @Mock ActivityRepository activityRepository;
    @Mock DocumentMapper documentMapper;
    @Mock DocumentAccessPolicy documentAccessPolicy;
    @InjectMocks DocumentServiceImpl service;

    @Test
    void createUsesAuthenticatedUserAsUploader() {
        User currentUser = User.builder().id(42L).role(UserRole.ADMIN).build();
        DocumentRequest request = new DocumentRequest(
                (short) 1, 10L, "  Policy document  ", null, 3L, null, null
        );

        when(documentAccessPolicy.currentUser()).thenReturn(currentUser);
        when(documentTypeRepository.findById((short) 1))
                .thenReturn(Optional.of(DocumentType.builder().id((short) 1).isActive(true).build()));
        when(fileRepository.existsById(10L)).thenReturn(true);
        when(branchRepository.existsById(3L)).thenReturn(true);
        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.createDocument(request);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).saveAndFlush(captor.capture());
        assertEquals(42L, captor.getValue().getUploadedById());
        assertEquals("Policy document", captor.getValue().getTitle());
        verify(documentAccessPolicy).requireOwnerAccess(currentUser, 3L, null, null);
    }
}
