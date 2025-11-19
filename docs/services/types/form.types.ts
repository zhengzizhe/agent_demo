import {DocumentType} from "./entity.types";

export interface ICreateFolderForm {
  name: string;
  targetId: string;
  description: string
}

export interface ICreateDocumentForm {
  name: string;
  type: DocumentType
  targetId: string;
}
