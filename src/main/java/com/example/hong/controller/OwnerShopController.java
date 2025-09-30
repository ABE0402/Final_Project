package com.example.hong.controller;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.dto.ShopCreateRequestDto;
import com.example.hong.entity.Cafe;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.TagRepository;
import com.example.hong.service.OwnerShopService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner/shops")
@PreAuthorize("hasRole('OWNER')")
@RequiredArgsConstructor
public class OwnerShopController {

    private final CafeRepository cafeRepository;
    private final OwnerShopService ownerShopService;
    private final TagRepository tagRepository;

    private Long meId(Authentication auth) {
        Object p = auth.getPrincipal();
        if (p instanceof AppUserPrincipal apr) return apr.getId();
        throw new IllegalStateException("인증 정보를 찾을 수 없습니다.");
    }
    private String nvl(String s){ return (s==null) ? "" : s; }

    /** 태그 리스트를 뷰 모델로 가공 (selected 플래그 포함) */
    private List<Map<String, Object>> tagListVm(String category, Set<Integer> selected) {
        return tagRepository.findByCategoryOrderByNameAsc(category)
                .stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "name", t.getName(),
                        "selected", selected != null && selected.contains(t.getId())
                ))
                .collect(Collectors.toList());
    }

    /** 폼에 뿌릴 태그 선택지 (선택 상태 반영) */
    private void populateTagLists(Model model, Set<Integer> selected) {
        model.addAttribute("companionTags",   tagListVm("companion",   selected));
        model.addAttribute("moodTags",        tagListVm("mood",        selected));
        model.addAttribute("amenityTags",     tagListVm("amenities",   selected));
        model.addAttribute("reservationTags", tagListVm("reservation", selected));
        model.addAttribute("priorityTags",    tagListVm("priority",    selected));
        model.addAttribute("typeTags",        tagListVm("type",        selected));
    }

    /** 내 매장 목록 */
    @GetMapping({"", "/"})
    public String index(Authentication auth, Model model,
                        @RequestParam(value="msg", required=false) String msg) {
        var cafes = cafeRepository.findByOwner_IdOrderByCreatedAtDesc(meId(auth));

        List<Map<String,Object>> shops = new ArrayList<>();
        for (Cafe c : cafes) {
            Map<String,Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("type", "CAFE");
            m.put("name", c.getName());
            m.put("address", nvl(c.getAddressRoad()));
            m.put("phone", nvl(c.getPhone()));
            m.put("heroImageUrl", c.getHeroImageUrl());

            ApprovalStatus st = c.getApprovalStatus();
            m.put("approvalStatus", st.name());
            m.put("isApproved", st == ApprovalStatus.APPROVED);
            m.put("isPending",  st == ApprovalStatus.PENDING);
            m.put("isRejected", st == ApprovalStatus.REJECTED);
            m.put("badgeClass",
                    (st == ApprovalStatus.APPROVED) ? "bg-success" :
                            (st == ApprovalStatus.PENDING)  ? "bg-warning text-dark" :
                                    "bg-danger");
            m.put("visible", c.isVisible());
            shops.add(m);
        }
        model.addAttribute("tabOwnerShops", true);
        model.addAttribute("shops", shops);
        if (msg != null) model.addAttribute("msg", msg);
        return "owner/shops_index";
    }

    /** 등록 폼 */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("tabOwnerShopsNew", true);
        model.addAttribute("form", new ShopCreateRequestDto()); // type은 POST에서 기본 CAFE로 보정
        populateTagLists(model, Collections.emptySet()); // 선택 없음
        return "owner/shops_new";
    }

    /** 등록 처리 */
    @PostMapping(value="/new", consumes = "multipart/form-data")
    public String create(Authentication auth,
                         @ModelAttribute ShopCreateRequestDto form,
                         RedirectAttributes ra) {
        if (form.getType() == null || form.getType().isBlank()) form.setType("CAFE");
        ownerShopService.createShop(meId(auth), form);
        ra.addAttribute("msg", "가게가 등록되었습니다. (승인 대기)");
        return "redirect:/owner/shops";
    }

    /** 수정 폼 */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        var vm = ownerShopService.getShopForEdit(meId(auth), id);
        vm.putIfAbsent("addressRoad", "");
        // select 표시용 플래그(원하면)
        String type = String.valueOf(vm.getOrDefault("type", "CAFE"));
        vm.put("typeIsCafe", "CAFE".equalsIgnoreCase(type));
        vm.put("typeIsRestaurant", "RESTAURANT".equalsIgnoreCase(type));
        model.addAttribute("tabOwnerShops", true);
        model.addAttribute("form", vm);

        // 선택된 태그 id 집합
        @SuppressWarnings("unchecked")
        var sel = (List<Integer>) vm.getOrDefault("selectedTagIds", List.of());

        populateTagLists(model, new HashSet<>(sel));// 선택 반영
        return "owner/shops_edit";
    }

    /** 수정 처리 */
    @PostMapping(value="/{id}/edit", consumes = "multipart/form-data")
    public String edit(@PathVariable Long id, Authentication auth,
                       @ModelAttribute ShopCreateRequestDto form, RedirectAttributes ra) {
        ownerShopService.updateShop(meId(auth), id, form);
        ra.addAttribute("msg", "가게 정보가 수정되었습니다.");
        return "redirect:/owner/shops";
    }

    /** 폐업(숨김) 신청 */
    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        ownerShopService.requestClose(meId(auth), id);
        ra.addAttribute("msg", "폐업(숨김) 처리되었습니다.");
        return "redirect:/owner/shops";
    }

    @PostMapping("/{id}/reopen")
    public String reopen(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        ownerShopService.reopen(meId(auth), id);
        ra.addAttribute("msg", "재오픈(표시) 처리되었습니다.");
        return "redirect:/owner/shops";
    }
}