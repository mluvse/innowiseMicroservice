package by.mluvse.UserService.web;

import by.mluvse.UserService.dto.CardDto;
import by.mluvse.UserService.exceptions.NotFoundException;
import by.mluvse.UserService.service.PaymentCardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/cards")
public class PaymentCardController {

    private final PaymentCardService cardService;

    public PaymentCardController(PaymentCardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardDto> create(@Valid @RequestBody CardDto cardDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.create(cardDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> get(@PathVariable Long id){
        return ResponseEntity.ok(cardService.getById(id).orElseThrow(() -> new NotFoundException("Card not found: " + id)));
    }

    @GetMapping
    public ResponseEntity<Page<CardDto>> getAll(@RequestParam(required = false) String name,
                                                @RequestParam(required = false) String surname,
                                                Pageable pageable){
        return ResponseEntity.ok(cardService.getAll(name,surname,pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardDto> update(@PathVariable Long id,
                                          @Valid @RequestBody CardDto dto) {
        return ResponseEntity.ok(cardService.update(id, dto));
    }
    @PatchMapping("/{id}/activate")
    public ResponseEntity<CardDto> activate(@PathVariable Long id){
        return ResponseEntity.ok(cardService.changeStatus(id, true));

    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CardDto> deactivate(@PathVariable Long id){
        return ResponseEntity.ok(cardService.changeStatus(id, false));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        cardService.delete(id);
        return ResponseEntity.ok().build();
    }
}
