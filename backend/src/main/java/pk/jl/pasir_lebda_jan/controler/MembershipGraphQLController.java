package pk.jl.pasir_lebda_jan.controler;

import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import pk.jl.pasir_lebda_jan.dto.GroupResponseDTO;
import pk.jl.pasir_lebda_jan.dto.MembershipDTO;
import pk.jl.pasir_lebda_jan.dto.MembershipResponseDTO;
import pk.jl.pasir_lebda_jan.model.Membership;
import pk.jl.pasir_lebda_jan.model.User;
import pk.jl.pasir_lebda_jan.repository.GroupRepository;
import pk.jl.pasir_lebda_jan.service.CurrentUserService;
import pk.jl.pasir_lebda_jan.service.MembershipService;

import java.util.List;

@Controller
public class MembershipGraphQLController {

    private final MembershipService membershipService;
    private final GroupRepository groupRepository;
    private final CurrentUserService currentUserService;

    public MembershipGraphQLController(
            MembershipService membershipService,
            GroupRepository groupRepository,
            CurrentUserService currentUserService) {
        this.membershipService = membershipService;
        this.groupRepository = groupRepository;
        this.currentUserService = currentUserService;
    }

    @QueryMapping
    public List<MembershipResponseDTO> groupMembers(@Argument Long groupId) {
        return membershipService.getGroupMembers(groupId).stream()
                .map(membership -> new MembershipResponseDTO(
                        membership.getId(),
                        membership.getUser().getId(),
                        membership.getGroup().getId(),
                        membership.getUser().getEmail()
                )).toList();
    }

    @MutationMapping
    public MembershipResponseDTO addMember(@Valid @Argument MembershipDTO membershipDTO) {
        Membership membership = membershipService.addMember(membershipDTO);
        return new MembershipResponseDTO(
                membership.getId(),
                membership.getUser().getId(),
                membership.getGroup().getId(),
                membership.getUser().getEmail()
        );
    }

    @QueryMapping
    public List<GroupResponseDTO> myGroups() {
        User currentUser = currentUserService.getCurrentUser();
        return groupRepository.findByMemberships_User(currentUser).stream()
                .map(group -> new GroupResponseDTO(
                        group.getId(),
                        group.getName(),
                        group.getOwner().getId()
                )).toList();
    }

    @MutationMapping
    public Boolean removeMember(@Argument Long membershipId) {
        membershipService.removeMember(membershipId);
        return true;
    }
}
