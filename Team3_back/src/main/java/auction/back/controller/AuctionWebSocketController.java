package auction.back.controller;

import auction.back.domain.Auction;
import auction.back.dto.request.BidAuctionRequestDto;
import auction.back.dto.response.socket.AuctionUpdateMessage;
import auction.back.repository.AuctionRepository;
import auction.back.service.AuctionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuctionWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final AuctionService auctionService;
    private final AuctionRepository auctionRepository;  // 추가

    @MessageMapping("/bid")
    public void handleBid(BidAuctionRequestDto bidRequest) {
        boolean result = auctionService.bidAuction(bidRequest);

        if (result) {
            // 경매 정보 조회
            Auction auction = auctionRepository.findById(bidRequest.auctionId())
                    .orElseThrow(() -> new EntityNotFoundException("Auction not found"));

            // 해당 경매의 토픽으로 메시지 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/auction/" + bidRequest.auctionId(),
                    new AuctionUpdateMessage(
                            bidRequest.auctionId(),
                            auction.getIngPrice(),
                            bidRequest.userId()
                    )
            );
        }
    }
}