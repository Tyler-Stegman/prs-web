package com.prs.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import com.prs.business.JsonResponse;
import com.prs.business.Request;
import com.prs.db.RequestRepository;

@RestController
@RequestMapping("/requests")
public class RequestController {

	@Autowired
	private RequestRepository requestRepo;

	@GetMapping("/")
	public JsonResponse list() {
		JsonResponse jr = null;
		List<Request> requests = requestRepo.findAll();
		if (requests.size() > 0) {
			jr = JsonResponse.getInstance(requests);
		} else {
			jr = JsonResponse.getErrorInstance("No requests found.");
		}
		return jr;
	}

	@GetMapping("/{id}")
	public JsonResponse get(@PathVariable int id) {
		JsonResponse jr = null;
		Optional<Request> request = requestRepo.findById(id);
		if (request.isPresent()) {
			jr = JsonResponse.getInstance(request.get());
		} else {
			jr = JsonResponse.getErrorInstance("No request found for id: " + id);
		}
		return jr;
	}

	@GetMapping("/list-review/{id}")
	public JsonResponse listRequestsInReviewStatus(@PathVariable int id) {
		JsonResponse jr = null;
		List<Request> requests = requestRepo.findByStatusAndUserIdNot("Review", id);
		try {
			if (requests.size() > 0) {
				jr = JsonResponse.getInstance(requests);
			} else {
				jr = JsonResponse.getErrorInstance("No requests in review status found for: " + id + ".");
			}
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error updating request: " + e.getMessage());
			e.printStackTrace();
		}
		return jr;
	}

	@PostMapping("/")
	public JsonResponse createRequest(@RequestBody Request r) {
		JsonResponse jr = null;
		r.setStatus("New");
		r.setSubmittedDate(LocalDateTime.now());
		try {
			r = requestRepo.save(r);
			jr = JsonResponse.getInstance(r);
		} catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error creating request: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	@PutMapping("/")
	public JsonResponse updateRequest(@RequestBody Request r) {
		JsonResponse jr = null;
		r.setSubmittedDate(LocalDateTime.now());
		try {
			r = requestRepo.save(r);
			jr = JsonResponse.getInstance(r);
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error updating request: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	@PutMapping("/submit-review")
	public JsonResponse submitRequestForReview(@RequestBody Request r) {
		JsonResponse jr = null;
		try {
			if (r.getTotal() <= 50.00) {
				r.setStatus("Approved");
			} else {
				r.setStatus("Review");
			}
			r.setSubmittedDate(LocalDateTime.now());
			r = requestRepo.save(r);
			jr = JsonResponse.getInstance(r);
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error updating request: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	@PutMapping("/approve")
	public JsonResponse requestApprove(@RequestBody Request r) {
		JsonResponse jr = null;
		try {
			r.setStatus("Approved");
			r.setSubmittedDate(LocalDateTime.now());
			r = requestRepo.save(r);
			jr = JsonResponse.getInstance(r);
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error approving request: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	@PutMapping("/reject")
	public JsonResponse requestReject(@RequestBody Request r) {
		JsonResponse jr = null;
		try {
			r.setStatus("Rejected");
			r.setSubmittedDate(LocalDateTime.now());
			r = requestRepo.save(r);
			jr = JsonResponse.getInstance(r);
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error rejecting request: " + e.getMessage());
			e.printStackTrace();
		}
		return jr;
	}

	@DeleteMapping("/{id}")
	public JsonResponse deleteRequest(@PathVariable int id) {
		JsonResponse jr = null;
		try {
			requestRepo.deleteById(id);
			jr = JsonResponse.getInstance("Request id: " + id + " deleted successfully.");
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error deleting request: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

}
