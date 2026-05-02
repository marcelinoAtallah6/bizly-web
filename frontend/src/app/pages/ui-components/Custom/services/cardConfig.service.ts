import { Card } from "./card.service";

export interface CardConfig {
  header: {
    titleField: keyof Card;
    subtitleField: keyof Card;
    menuOptions: { icon: string; label: string; action: string }[];
  };
  content: {
    description: keyof Card;
    fields: { label: string; field: keyof Card; format: keyof Card; }[];
  };
  actions: {
    buttons: { icon: string; label: string; action: string }[];
    span: { label: string; field: string; format?: string; }[];

  };
  customDetailScreen: any;
  generalSettings: {
    imageField: keyof Card;
    dateField: keyof Card;
    idField: keyof Card;
  };
  generalConfiguration: {
    showHeader: boolean;
    showContent: boolean;
    showAction: boolean;
    showImage: boolean;
    showSubTitleField: boolean;
    showTitleField: boolean;
    showMenuOption: boolean;
    showActionButtons: boolean;
    withCustomDetailScreen:boolean;

  };
}