import { EngageBySailthru } from "../src/index";

describe("AttributeMap", () => {
  var attributeMap;
  beforeEach(() => {
    attributeMap = new EngageBySailthru.AttributeMap();
  });

  describe("when created", () => {
    it("defaults to merge rule Update", () => {
      expect(attributeMap.mergeRule).toEqual(attributeMap.MergeRules.Update);
    });
  });

  describe("getAttributes", () => {
    var attributes;

    describe("when attributes are empty", () => {
      beforeEach(() => {
        attributes = attributeMap.getAttributes();
      });

      it("contains empty attributes", () => {
        expect(attributes.attributes).toEqual({});
      });

      it("contains merge rule", () => {
        expect(attributes.mergeRule).toEqual(attributeMap.MergeRules.Update);
      });
    });

    describe("when attributes are not empty", () => {
      beforeEach(() => {
        attributeMap.setString("name", "value");
        attributes = attributeMap.getAttributes();
      });

      it("contains attributes", () => {
        expect(attributes.attributes).toEqual({
          name: { type: "string", value: "value" },
        });
      });

      it("contains merge rule", () => {
        expect(attributes.mergeRule).toEqual(attributeMap.MergeRules.Update);
      });
    });
  });

  describe("get", () => {
    beforeEach(() => {
      attributeMap.setString("present", "value");
    });

    describe("when key has matching value", () => {
      it("returns value for key", () => {
        expect(attributeMap.get("present")).toEqual({
          type: "string",
          value: "value",
        });
      });
    });

    describe("when key does not have matching value", () => {
      it("returns value for key", () => {
        expect(attributeMap.get("not-present")).toEqual(null);
      });
    });
  });

  describe("remove", () => {
    beforeEach(() => {
      attributeMap.setString("present", "value");
    });

    describe("when key has matching value", () => {
      it("removes value for key", () => {
        expect(attributeMap.attributes).toEqual({
          present: { type: "string", value: "value" },
        });
        attributeMap.remove("present");
        expect(attributeMap.attributes).toEqual({});
      });
    });

    describe("when key does not have matching value", () => {
      it("leaves attributes unchanged", () => {
        expect(attributeMap.attributes).toEqual({
          present: { type: "string", value: "value" },
        });
        attributeMap.remove("not-present");
        expect(attributeMap.attributes).toEqual({
          present: { type: "string", value: "value" },
        });
      });
    });
  });

  describe("setMergeRule", () => {
    describe("when valid mergeRule is supplied", () => {
      beforeEach(() => {
        attributeMap.setMergeRule(attributeMap.MergeRules.Replace);
      });

      it("updates the merge rule", () => {
        expect(attributeMap.mergeRule).toEqual(attributeMap.MergeRules.Replace);
      });
    });

    describe("when invalid mergeRule is supplied", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setMergeRule("not-a-merge-rule");
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setString", () => {
    describe("when valid string is added", () => {
      beforeEach(() => {
        attributeMap.setString("test", "me");
      });

      it("sets the correct type", () => {
        expect(attributeMap.attributes["test"]["type"]).toEqual("string");
      });

      it("contains the correct value", () => {
        expect(attributeMap.attributes["test"]["value"]).toEqual("me");
      });
    });

    describe("when wrong type is added", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setString("test", 1);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setStringArray", () => {
    describe("when valid string array is added", () => {
      beforeEach(() => {
        attributeMap.setStringArray("test", ["me"]);
      });

      it("sets the correct type", () => {
        expect(attributeMap.attributes["test"]["type"]).toEqual("stringArray");
      });

      it("contains the correct value", () => {
        expect(attributeMap.attributes["test"]["value"][0]).toEqual("me");
      });
    });

    describe("when wrong type is added", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setStringArray("test", "me");
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when wrong type is added in array", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setStringArray("test", [567]);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setInteger", () => {
    describe("when valid integer is added", () => {
      beforeEach(() => {
        attributeMap.setInteger("test", 567);
      });

      it("sets the correct type", () => {
        expect(attributeMap.attributes["test"]["type"]).toEqual("integer");
      });

      it("contains the correct value", () => {
        expect(attributeMap.attributes["test"]["value"]).toEqual(567);
      });
    });

    describe("when wrong type is added", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setInteger("test", "me");
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when non-integer is added", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setInteger("test", 5.67);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setIntegerArray", () => {
    describe("when valid integer array is added", () => {
      beforeEach(() => {
        attributeMap.setIntegerArray("test", [567]);
      });

      it("sets the correct type", () => {
        expect(attributeMap.attributes["test"]["type"]).toEqual("integerArray");
      });

      it("contains the correct value", () => {
        expect(attributeMap.attributes["test"]["value"][0]).toEqual(567);
      });
    });

    describe("when wrong type is added", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setIntegerArray("test", 567);
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when wrong type is added in array", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setIntegerArray("test", ["me"]);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setBoolean", () => {
    describe("when valid boolean is added", () => {
      beforeEach(() => {
        attributeMap.setBoolean("test", true);
      });

      it("sets the correct type", () => {
        expect(attributeMap.attributes["test"]["type"]).toEqual("boolean");
      });

      it("contains the correct value", () => {
        expect(attributeMap.attributes["test"]["value"]).toEqual(true);
      });
    });

    describe("when wrong type is added", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setBoolean("test", "me");
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setFloat", () => {
    describe("when valid float is added", () => {
      beforeEach(() => {
        attributeMap.setFloat("test", 5.67);
      });

      it("sets the correct type", () => {
        expect(attributeMap.attributes["test"]["type"]).toEqual("float");
      });

      it("contains the correct value", () => {
        expect(attributeMap.attributes["test"]["value"]).toEqual(5.67);
      });
    });

    describe("when wrong type is added", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setFloat("test", "me");
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setFloatArray", () => {
    describe("when valid float array is added", () => {
      beforeEach(() => {
        attributeMap.setFloatArray("test", [5.67]);
      });

      it("sets the correct type", () => {
        expect(attributeMap.attributes["test"]["type"]).toEqual("floatArray");
      });

      it("contains the correct value", () => {
        expect(attributeMap.attributes["test"]["value"][0]).toEqual(5.67);
      });
    });

    describe("when wrong type is added", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setFloatArray("test", "me");
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when wrong type is added in array", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setFloatArray("test", ["me"]);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setDate", () => {
    describe("when valid date is added", () => {
      let date = new Date();

      beforeEach(() => {
        attributeMap.setDate("test", date);
      });

      it("sets the correct type", () => {
        expect(attributeMap.attributes["test"]["type"]).toEqual("date");
      });

      it("contains the correct value", () => {
        expect(attributeMap.attributes["test"]["value"]).toEqual(
          date.getTime()
        );
      });
    });

    describe("when wrong type is added", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setDate("test", "me");
        };
        expect(run).toThrow(TypeError);
      });
    });
  });

  describe("setDateArray", () => {
    describe("when valid date array is added", () => {
      let date = new Date();

      beforeEach(() => {
        attributeMap.setDateArray("test", [date]);
      });

      it("sets the correct type", () => {
        expect(attributeMap.attributes["test"]["type"]).toEqual("dateArray");
      });

      it("contains the correct value", () => {
        expect(attributeMap.attributes["test"]["value"][0]).toEqual(
          date.getTime()
        );
      });
    });

    describe("when wrong type is added", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setDateArray("test", "me");
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when wrong type is added in array", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setDateArray("test", ["me"]);
        };
        expect(run).toThrow(TypeError);
      });
    });

    describe("when invalid date is added in array", () => {
      it("throws a type error", () => {
        const run = () => {
          attributeMap.setDateArray("test", [new Date("iusahfijgbn")]);
        };
        expect(run).toThrow(TypeError);
      });
    });
  });
});