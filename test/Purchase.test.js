import SailthruMobile from "../index";

describe("Purchase", () => {
  var purchase;
  beforeEach(() => {
    purchase = new SailthruMobile.Purchase([{ fake: "item" }]);
  });

  describe("when created", () => {
    describe("when value contains purchase items", () => {
      it("should set the purchase items", () => {
        expect(purchase.items).toEqual([{ fake: "item" }]);
      });
    });

    describe("when value is not an array", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchase = new SailthruMobile.Purchase(1234);
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when value does not contain purchase items", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchase = new SailthruMobile.Purchase([1234, 1234]);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setVars", () => {
    describe("when value is an object", () => {
      beforeEach(() => {
        purchase.setVars({ some: "var" });
      });

      it("should set the vars", () => {
        expect(purchase.vars).toEqual({ some: "var" });
      });
    });

    describe("when value is not an object", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchase.setVars(1234);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setMessageId", () => {
    describe("when value is a string", () => {
      beforeEach(() => {
        purchase.setMessageId("messageID");
      });

      it("should set the message_id", () => {
        expect(purchase.message_id).toEqual("messageID");
      });
    });

    describe("when value is not a string", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchase.setMessageId(1234);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setPurchaseAdjustments", () => {
    describe("when value contains purchase adjustments", () => {
      beforeEach(() => {
        purchase.setPurchaseAdjustments([
          new SailthruMobile.PurchaseAdjustment("tax", 1234),
        ]);
      });

      it("should set the purchase adjustments", () => {
        expect(purchase.adjustments).toEqual([{ title: "tax", price: 1234 }]);
      });
    });

    describe("when value is not an array", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchase.setPurchaseAdjustments(1234);
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when value does not contain purchase adjustments", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchase.setPurchaseAdjustments([1234, 1234]);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });
});
