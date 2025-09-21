import { EngageBySailthru } from "../src/index";

describe("PurchaseAdjustment", () => {
  var purchaseAdjustment = new EngageBySailthru.PurchaseAdjustment(
    "myTitle",
    3456
  );

  describe("when created", () => {
    it("should set the title", () => {
      expect(purchaseAdjustment.title).toEqual("myTitle");
    });

    it("should set the price", () => {
      expect(purchaseAdjustment.price).toEqual(3456);
    });

    describe("when price is negative", () => {
      beforeEach(() => {
        purchaseAdjustment = new EngageBySailthru.PurchaseAdjustment(
          "myTitle",
          -3456
        );
      });

      it("should set the title", () => {
        expect(purchaseAdjustment.title).toEqual("myTitle");
      });

      it("should set the price", () => {
        expect(purchaseAdjustment.price).toEqual(-3456);
      });
    });

    describe("when title is not a string", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseAdjustment = new EngageBySailthru.PurchaseAdjustment(
            1234,
            3456
          );
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when price is not an integer", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseAdjustment = new EngageBySailthru.PurchaseAdjustment(
            1234,
            3.456
          );
        };
        expect(run).toThrow(TypeError);
      });
    });
  });
});
