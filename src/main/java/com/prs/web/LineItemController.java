package com.prs.web;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import com.prs.business.JsonResponse;
import com.prs.business.LineItem;
import com.prs.business.Request;
import com.prs.db.LineItemRepository;
import com.prs.db.RequestRepository;

@RestController
@RequestMapping("/line-items")
public class LineItemController {

	@Autowired
	private LineItemRepository lineItemRepo;

	@Autowired
	private RequestRepository requestRepo;

	@GetMapping("/")
	public JsonResponse list() {
		JsonResponse jr = null;
		List<LineItem> lineItems = lineItemRepo.findAll();
		if (lineItems.size() > 0) {
			jr = JsonResponse.getInstance(lineItems);
		} else {
			jr = JsonResponse.getErrorInstance("No line items found.");
		}
		return jr;
	}

	@GetMapping("/{id}")
	public JsonResponse get(@PathVariable int id) {
		JsonResponse jr = null;
		Optional<LineItem> lineItem = lineItemRepo.findById(id);
		if (lineItem.isPresent()) {
			jr = JsonResponse.getInstance(lineItem.get());
		} else {
			jr = JsonResponse.getErrorInstance("No line item found for id: " + id);
		}
		return jr;
	}

	@GetMapping("/lines-for-pr/{id}")
	public JsonResponse listLineItemsForAPr(@PathVariable int id) {
		JsonResponse jr = null;
		List<LineItem> lineItems = lineItemRepo.findAllByRequestId(id);
		try {
			if (lineItems.size() > 0) {
				jr = JsonResponse.getInstance(lineItems);
			} else {
				jr = JsonResponse.getErrorInstance("No line items to list for request id: " + id + ".");
			}
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error finding line items: " + e.getMessage());
			e.printStackTrace();
		}
		return jr;
	}

	@PostMapping("/")
	public JsonResponse createLineItem(@RequestBody LineItem li) {
		JsonResponse jr = null;
		try {
			li = lineItemRepo.save(li);
			jr = JsonResponse.getInstance(li);
		} catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error creating line item: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	@PostMapping("/line-items")
	public JsonResponse lineItemRecalculate(@RequestBody LineItem li) {
		JsonResponse jr = null;
		try {
			jr = JsonResponse.getInstance(lineItemRepo.save(li));
			recalculateTotal(li.getRequest());
		} catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error recalculating line item: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	@PutMapping("/")
	public JsonResponse updateLineItem(@RequestBody LineItem li) {
		JsonResponse jr = null;
		try {
			li = lineItemRepo.save(li);
			jr = JsonResponse.getInstance(li);
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error updating line item: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	@PutMapping("/line-items")
	public JsonResponse updateRecalculatedLineItem(@RequestBody LineItem li) {
		JsonResponse jr = null;
		try {
			if (lineItemRepo.existsById(li.getId())) {
				jr = JsonResponse.getInstance(lineItemRepo.save(li));
				recalculateTotal(li.getRequest());
			} else {
				jr = JsonResponse.getErrorInstance("Line item id: " + li.getId() + "does not exist.");
			}
		} catch (Exception e) {
			jr = JsonResponse.getInstance(e);
			e.printStackTrace();
		}

		return jr;
	}

	@DeleteMapping("/{id}")
	public JsonResponse deleteLineItem(@PathVariable int id) {
		JsonResponse jr = null;
		try {
			lineItemRepo.deleteById(id);
			jr = JsonResponse.getInstance("Line item id: " + id + " deleted successfully.");
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error deleting line item: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	@DeleteMapping("/line-items")
	public JsonResponse deleteRecalculatedLineItem(@PathVariable int id) {
		JsonResponse jr = null;
		try {
			LineItem li = lineItemRepo.findById(id).get();
			lineItemRepo.deleteById(id);
			jr = JsonResponse.getInstance("Line item id: " + id + " deleted successfully.");
			Request r = li.getRequest();
			recalculateTotal(r);
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error deleting line item: " + e.getMessage());
			e.printStackTrace();
		}

		return jr;
	}

	private void recalculateTotal(Request r) {
		List<LineItem> lineItem = lineItemRepo.findAllByRequestId(r.getId());
		double total = 0.0;
		for (LineItem line : lineItem) {
			total += line.getQuantity() * line.getProduct().getPrice();
		}
		r.setTotal(total);
		try {
			requestRepo.save(r);
		} catch (Exception e) {
			throw e;
		}
	}

}
