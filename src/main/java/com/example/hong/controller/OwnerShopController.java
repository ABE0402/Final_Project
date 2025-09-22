package com.example.hong.controller;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.dto.ShopCreateRequestDto;
import com.example.hong.entity.Cafe;
import com.example.hong.repository.CafeRepository;
import com.example.hong.service.OwnerShopService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/owner/shops")
@PreAuthorize("hasRole('OWNER')")
@RequiredArgsConstructor
public class OwnerShopController {

    private final CafeRepository cafeRepository;
    private final OwnerShopService ownerShopService;

    private Long meId(Authentication auth) { return ((AppUserPrincipal) auth.getPrincipal()).getId(); }
    private String nvl(String s){ return (s==null) ? "" : s; }

    /** 내 매장 목록 */
    @GetMapping({"", "/"})
    public String index(Authentication auth, Model model,
                        @RequestParam(value="msg", required=false) String msg) {

        var cafes = cafeRepository.findByOwner_IdOrderByCreatedAtDesc(meId(auth));

        // jmustache용 view-model (equals 헬퍼 없이 boolean 플래그로)
        List<Map<String,Object>> shops = new ArrayList<>();
        for (Cafe c : cafes) {
            Map<String,Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("type", "CAFE");
            m.put("name", c.getName());
            m.put("address", nvl(c.getAddressRoad()));
            m.put("phone", nvl(c.getPhone()));
            m.put("heroImageUrl", c.getHeroImageUrl());

            ApprovalStatus st = c.getApprovalStatus();    // ✅ 공용 enum
            m.put("approvalStatus", st.name());
            m.put("isApproved", st == ApprovalStatus.APPROVED);  // ✅ 비교도 공용 enum
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

    /** ⬇⬇⬇ 라우팅 빠져있던 부분 추가 ⬇⬇⬇ */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("tabOwnerShopsNew", true); // 사이드바 활성화용 (옵션)
        model.addAttribute("form", new ShopCreateRequestDto());
        return "owner/shops_new";
    }

    /** 등록 처리 */
    @PostMapping(value="/new", consumes = "multipart/form-data")
    public String create(Authentication auth,
                         @ModelAttribute ShopCreateRequestDto form,
                         RedirectAttributes ra) {
        ownerShopService.createShop(meId(auth), form);
        ra.addAttribute("msg", "가게가 등록되었습니다. (승인 대기)");
        return "redirect:/owner/shops";
    }

    /** 수정 폼 */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        var vm = ownerShopService.getShopForEdit(meId(auth), id);
        model.addAttribute("tabOwnerShops", true);
        model.addAttribute("form", vm);
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
}