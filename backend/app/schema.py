from typing import Annotated, Literal

from pydantic import BaseModel, ConfigDict, Field, StringConstraints, model_validator

Text = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=900)]
ShortText = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=160)]
Steps = Annotated[list[Text], Field(max_length=6)]


class Contract(BaseModel):
    model_config = ConfigDict(extra="forbid", strict=True)


class Identification(Contract):
    object_name: ShortText | None
    material: ShortText | None
    category: Literal["Organik", "Anorganik", "B3", "Residu", "Tidak Diketahui"] | None
    recyclable: bool | None
    recycling_status: Literal["recyclable", "conditionally_recyclable", "non_recyclable", "unknown"]
    tags: Annotated[list[ShortText], Field(max_length=5)]
    description: Text | None
    requires_verification: bool
    verification_note: Text | None
    hazardous: bool

    @model_validator(mode="after")
    def consistent(self):
        expected = {"recyclable": True, "conditionally_recyclable": True, "non_recyclable": False, "unknown": None}
        if self.recyclable is not expected[self.recycling_status]:
            raise ValueError("recyclable does not match recycling_status")
        if self.requires_verification and not self.verification_note:
            raise ValueError("verification_note is required")
        if self.category == "B3" and not self.hazardous:
            raise ValueError("B3 must be hazardous")
        if self.recycling_status == "unknown" and not self.requires_verification:
            raise ValueError("unknown recycling requires verification")
        return self


class Idea(Contract):
    title: ShortText
    description: Text
    difficulty: Literal["Mudah", "Sedang", "Sulit"]
    tools_and_materials: Annotated[list[ShortText], Field(min_length=1, max_length=12)]
    steps: Annotated[list[Text], Field(min_length=3, max_length=6)]
    safety_warning: Text


class Upcycling(Contract):
    possible: bool
    reason: Text | None
    ideas: Annotated[list[Idea], Field(max_length=3)]

    @model_validator(mode="after")
    def consistent(self):
        if self.possible != bool(self.ideas):
            raise ValueError("possible must match ideas")
        if not self.possible and not self.reason:
            raise ValueError("reason is required when upcycling is unavailable")
        if len({idea.title.casefold() for idea in self.ideas}) != len(self.ideas):
            raise ValueError("duplicate ideas")
        return self


class IndustrialRecycling(Contract):
    possible: bool | None
    description: Text | None
    potential_products: Annotated[list[ShortText], Field(max_length=6)]
    limitations: Steps

    @model_validator(mode="after")
    def consistent(self):
        if self.possible is not True and self.potential_products:
            raise ValueError("unavailable recycling cannot promise products")
        return self


class DepositRecommendation(Contract):
    recommended: bool
    partner_type: ShortText | None
    search_keyword: ShortText | None
    preparation_note: Text | None

    @model_validator(mode="after")
    def consistent(self):
        if self.recommended and (not self.partner_type or not self.search_keyword):
            raise ValueError("recommended deposits require a partner type and search keyword")
        return self


class ScanResponse(Contract):
    status: Literal["success", "invalid_image", "multiple_objects"]
    message: Text | None
    identification: Identification | None
    handling_steps: Steps
    upcycling: Upcycling
    industrial_recycling: IndustrialRecycling | None
    deposit_recommendation: DepositRecommendation | None

    @model_validator(mode="after")
    def consistent(self):
        if self.status != "success":
            if not self.message or self.identification is not None:
                raise ValueError("failed identification requires a message and null identification")
            if self.handling_steps or self.upcycling.possible or self.upcycling.ideas:
                raise ValueError("failed identification cannot contain recommendations")
            if self.industrial_recycling is not None or self.deposit_recommendation is not None:
                raise ValueError("failed identification cannot recommend processing or deposits")
            return self
        identity = self.identification
        if not identity or not identity.object_name or not identity.category or not identity.description:
            raise ValueError("success requires an identification")
        if not self.handling_steps or not self.industrial_recycling or not self.deposit_recommendation:
            raise ValueError("success requires handling, industry and deposit sections")
        if identity.hazardous and (self.upcycling.possible or self.upcycling.ideas):
            raise ValueError("hazardous waste cannot receive DIY guides")
        primary_object = (identity.object_name + " " + (identity.material or "")).casefold()
        unsafe_objects = ("baterai", "battery", "aki ", "jarum suntik", "limbah medis", "pestisida", "limbah elektronik", "komponen elektronik")
        if any(term in primary_object for term in unsafe_objects):
            if not identity.hazardous or self.upcycling.ideas:
                raise ValueError("unsafe objects require hazardous handling without DIY guides")
        if identity.requires_verification or identity.material is None:
            food_uses = ("wadah makanan", "air minum", "gelas minum", "botol minum")
            for idea in self.upcycling.ideas:
                use = (idea.title + " " + idea.description).casefold()
                if any(term in use for term in food_uses):
                    raise ValueError("unverified material cannot be reused for food or drinking water")
        if not self.industrial_recycling.description:
            raise ValueError("industrial recycling requires an explanation")
        if identity.recycling_status == "non_recyclable" and self.industrial_recycling.possible is True:
            raise ValueError("non-recyclable waste cannot promise industrial recycling")
        if identity.recycling_status == "conditionally_recyclable" and not self.industrial_recycling.limitations:
            raise ValueError("conditional recycling requires limitations")
        return self
