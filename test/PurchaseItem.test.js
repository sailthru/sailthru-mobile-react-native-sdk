import SailthruMobile from "../index";

describe("PurchaseItem", () => {
  var purchaseItem;

  beforeEach(() => {
    purchaseItem = new SailthruMobile.PurchaseItem(
      2,
      "myTitle",
      3456,
      "id",
      "url"
    );
  });

  describe("when created", () => {
    it("should set the quantity", () => {
      expect(purchaseItem.qty).toEqual(2);
    });

    it("should set the title", () => {
      expect(purchaseItem.title).toEqual("myTitle");
    });

    it("should set the price", () => {
      expect(purchaseItem.price).toEqual(3456);
    });

    it("should set the ID", () => {
      expect(purchaseItem.id).toEqual("id");
    });

    it("should set the URL", () => {
      expect(purchaseItem.url).toEqual("url");
    });

    describe("when quantity is wrong type", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseItem = new SailthruMobile.PurchaseItem(
            "2",
            "myTitle",
            3456,
            "id",
            "url"
          );
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when quantity is not an integer", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseItem = new SailthruMobile.PurchaseItem(
            2.5,
            "myTitle",
            3456,
            "id",
            "url"
          );
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when title is not a string", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseItem = new SailthruMobile.PurchaseItem(
            2,
            55,
            3456,
            "id",
            "url"
          );
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when price is wrong type", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseItem = new SailthruMobile.PurchaseItem(
            2,
            "myTitle",
            "3456",
            "id",
            "url"
          );
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when price is not an integer", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseItem = new SailthruMobile.PurchaseItem(
            "2",
            "myTitle",
            34.56,
            "id",
            "url"
          );
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when id is not a string", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseItem = new SailthruMobile.PurchaseItem(
            2,
            "myTitle",
            3456,
            123,
            "url"
          );
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when url is not a string", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseItem = new SailthruMobile.PurchaseItem(
            2,
            "myTitle",
            3456,
            "id",
            123
          );
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setTags", () => {
    describe("when valid string array is added", () => {
      beforeEach(() => {
        purchaseItem.setTags(["tag!"]);
      });

      it("should set the tags", () => {
        expect(purchaseItem.tags[0]).toEqual("tag!");
      });
    });

    describe("when wrong type is added", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseItem.setTags({});
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when wrong type is added in array", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseItem.setTags([1234]);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setVars", () => {
    describe("when value is an object", () => {
      beforeEach(() => {
        purchaseItem.setVars({ some: "var" });
      });

      it("should set the vars", () => {
        expect(purchaseItem.vars).toEqual({ some: "var" });
      });
    });

    describe("when value is not an object", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseItem.setVars(1234);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setImages", () => {
    describe("when both full and thumb are present", () => {
      let images = {
        full: { url: "www.some-full-url.com" },
        thumb: { url: "www.some-thumb-url.com" },
      };

      beforeEach(() => {
        purchaseItem.setImages(images);
      });

      it("should set the full image", () => {
        expect(purchaseItem.images.full.url).toEqual("www.some-full-url.com");
      });

      it("should set the thumb image", () => {
        expect(purchaseItem.images.thumb.url).toEqual("www.some-thumb-url.com");
      });
    });

    describe("when only full is present", () => {
      let images = { full: { url: "www.some-full-url.com" } };

      beforeEach(() => {
        purchaseItem.setImages(images);
      });

      it("should set the full image", () => {
        expect(purchaseItem.images.full.url).toEqual("www.some-full-url.com");
      });

      it("should not set the thumb image", () => {
        expect(purchaseItem.images.thumb).toEqual(undefined);
      });
    });

    describe("when only thumb is present", () => {
      let images = { thumb: { url: "www.some-thumb-url.com" } };

      beforeEach(() => {
        purchaseItem.setImages(images);
      });

      it("should set the full image", () => {
        expect(purchaseItem.images.full).toEqual(undefined);
      });

      it("should set the thumb image", () => {
        expect(purchaseItem.images.thumb.url).toEqual("www.some-thumb-url.com");
      });
    });

    describe("when another item is present", () => {
      let images = {
        full: { url: "www.some-full-url.com" },
        thumb: { url: "www.some-thumb-url.com" },
        other: {},
      };

      beforeEach(() => {
        purchaseItem.setImages(images);
      });

      it("should set the full image", () => {
        expect(purchaseItem.images.full.url).toEqual("www.some-full-url.com");
      });

      it("should set the thumb image", () => {
        expect(purchaseItem.images.thumb.url).toEqual("www.some-thumb-url.com");
      });

      it("should not set the other item", () => {
        expect(purchaseItem.images.other).toEqual(undefined);
      });
    });

    describe("when value is not an object", () => {
      it("should throw a type error", () => {
        const run = () => {
          purchaseItem.setImages(1234);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });
});
